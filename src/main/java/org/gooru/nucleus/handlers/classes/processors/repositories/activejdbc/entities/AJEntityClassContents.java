package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Table("class_contents")
public class AJEntityClassContents extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityClassContents.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final String CREATED_AT = "created_at";
  private static final String UPDATED_AT = "updated_at";
  private static final String CLASS_ID = "class_id";
  public static final String FOR_MONTH = "for_month";
  public static final String FOR_YEAR = "for_year";
  public static final String CONTENT_ID = "content_id";
  public static final String CONTENT_TYPE = "content_type";
  public static final String ACTIVATION_DATE = "activation_date";
  public static final String DCA_ADDED_DATE = "dca_added_date";
  public static final String ALLOW_MASTERY_ACCRUAL = "allow_mastery_accrual";
  public static final String ASSESSMENT = "assessment";
  public static final String ASSESSMENT_EXTERNAL = "assessment-external";
  public static final String COLLECTION_EXTERNAL = "collection-external";
  public static final String COLLECTION = "collection";
  public static final String RESOURCE = "resource";
  public static final String QUESTION = "question";
  public static final String ID_CONTENT = "contentId";
  private static final String SORT_DESC = " desc";
  public static final String USERS = "users";
  public static final String USERS_COUNT = "users_count";
  public static final String ID = "id";
  private static final String COMMA_SEPARATOR = ",";
  public static final Pattern ASSESSMENT_TYPES = Pattern.compile("assessment|assessment-external");
  public static final Pattern COLLECTION_TYPES = Pattern.compile("collection|collection-external");

  private static final Set<String> CREATABLE_FIELDS = new HashSet<>(Arrays
      .asList(ID, CLASS_ID, FOR_MONTH, FOR_YEAR, CONTENT_ID, CONTENT_TYPE, CREATED_AT, UPDATED_AT,
          DCA_ADDED_DATE, ALLOW_MASTERY_ACCRUAL));
  private static final Set<String> UPDATE_USERS_FIELDS = new HashSet<>(Arrays
      .asList(USERS));
  private static final Set<String> UPDATEABLE_FIELDS = new HashSet<>(
      Arrays.asList(ACTIVATION_DATE, UPDATED_AT));
  private static final Set<String> MANDATORY_FIELDS =
      new HashSet<>(Arrays.asList(CONTENT_ID, CONTENT_TYPE, FOR_MONTH, FOR_YEAR));
  private static final Set<String> ACCEPT_CONTENT_TYPES = new HashSet<>(Arrays
      .asList(ASSESSMENT, COLLECTION, ASSESSMENT_EXTERNAL, COLLECTION_EXTERNAL, RESOURCE,
          QUESTION));
  public static final List<String> RESPONSE_FIELDS_FOR_TEACHER = Arrays
      .asList(ID, CONTENT_ID, CONTENT_TYPE, FOR_YEAR, FOR_MONTH, DCA_ADDED_DATE, ACTIVATION_DATE,
          CREATED_AT, USERS_COUNT, ALLOW_MASTERY_ACCRUAL);
  public static final List<String> RESPONSE_FIELDS_FOR_STUDENT = Arrays
      .asList(ID, CONTENT_ID, CONTENT_TYPE, FOR_YEAR, FOR_MONTH, DCA_ADDED_DATE, ACTIVATION_DATE,
          CREATED_AT, ALLOW_MASTERY_ACCRUAL);
  private static final Map<String, FieldValidator> validatorRegistry;
  private static final Map<String, FieldConverter> converterRegistry;

  public static final String SELECT_CLASS_CONTENTS_TO_VALIDATE =
      "select class_id, content_id from class_contents where class_id = ?::uuid AND content_id = ?::uuid AND "
          + "activation_date = ?::date";

  public static final String SELECT_DUPLICATED_ADDED_CONTENT =
      "class_id = ?::uuid and content_id = ?::uuid and content_type = ? and dca_added_date::DATE = ?::DATE";

  public static final String SELECT_DUPLICATED_ADDED_CONTENT_FOR_MONTH =
      "class_id = ?::uuid and content_id = ?::uuid and content_type = ? and dca_added_date::DATE is null "
          + " and for_month = ? and for_year = ?";

  private static final String SELECT_CLASS_CONTENTS =
      "class_id = ?::uuid AND for_year = ? AND for_month = ?";

  private static final String SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED =
      "class_id = ?::uuid AND activation_date BETWEEN ?::date AND ?::date and (?::text = any(users) OR users is null)";

  private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE =
      "class_id = ?::uuid AND for_year = ? and for_month = ? and content_type = ? ";

  private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED =
      "class_id = ?::uuid AND content_type = ? AND activation_date BETWEEN ?::date AND ?::date AND (?::text = any(users) OR users is null)";

  private static final String UPDATE_CLASS_CONTENTS_USERS = "update class_contents set users = ?::text[], users_count = ? where id = ?";

  public static final String FETCH_CLASS_CONTENT = "id = ?::bigint AND class_id = ?::uuid";

  static {
    validatorRegistry = initializeValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap
        .put(CLASS_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap
        .put(CONTENT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(ACTIVATION_DATE,
        (fieldValue -> FieldConverter
            .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    converterMap.put(DCA_ADDED_DATE,
        (fieldValue -> FieldConverter
            .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    return Collections.unmodifiableMap(converterMap);
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(CLASS_ID, (FieldValidator::validateUuid));
    validatorMap.put(CONTENT_ID, (FieldValidator::validateUuid));
    validatorMap
        .put(FOR_MONTH, FieldValidator::validateMonth);
    validatorMap.put(FOR_YEAR, FieldValidator::validateYear);
    validatorMap
        .put(CONTENT_TYPE,
            (value -> FieldValidator.validateValueExists((String) value, ACCEPT_CONTENT_TYPES)));
    validatorMap.put(DCA_ADDED_DATE, (value -> FieldValidator
        .validateDateWithFormatWithInDaysBoundary(value, DateTimeFormatter.ISO_LOCAL_DATE, 1)));
    validatorMap.put(USERS,
        (value) -> FieldValidator
            .validateDeepJsonArrayIfPresentAllowEmpty(value, FieldValidator::validateUuid));
    validatorMap.put(ALLOW_MASTERY_ACCRUAL, FieldValidator::validateBooleanIfPresent);
    return Collections.unmodifiableMap(validatorMap);
  }

  public static FieldSelector createFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(CREATABLE_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MANDATORY_FIELDS);
      }
    };
  }

  public static FieldSelector updateUsersFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(UPDATE_USERS_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(UPDATE_USERS_FIELDS);
      }
    };
  }


  public static FieldSelector updateFieldSelector() {
    return () -> Collections.unmodifiableSet(UPDATEABLE_FIELDS);
  }

  public void setClassId(String classId) {
    if (classId != null && !classId.isEmpty()) {
      PGobject value = FieldConverter.convertFieldToUuid(classId);
      if (value != null) {
        this.set(CLASS_ID, value);
      } else {
        LOGGER.warn("Not able to set class id as '{}' for create class content", classId);
        this.errors().put(CLASS_ID, RESOURCE_BUNDLE.getString("invalid.class.for.content.create"));

      }
    }
  }

  public void setActivationDateIfNotPresent(LocalDate activationDate) {
    if (this.getDate(ACTIVATION_DATE) == null) {
      this.set(ACTIVATION_DATE,
          FieldConverter
              .convertFieldToDateWithFormat(activationDate, DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }

  public void setDcaAddedDateIfNotPresent(LocalDate dcaAddedDate) {
    if (this.getDate(DCA_ADDED_DATE) == null) {
      this.set(DCA_ADDED_DATE,
          FieldConverter
              .convertFieldToDateWithFormat(dcaAddedDate, DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }


  public Date getActivationDate() {
    return this.getDate(ACTIVATION_DATE);
  }

  public String getContentId() {
    return this.getString(CONTENT_ID);
  }

  public String getContentType() {
    return this.getString(CONTENT_TYPE);
  }

  public Date getCreatedDate() {
    return this.getDate(CREATED_AT);
  }

  public Date getDcaAddedDate() {
    return this.getDate(DCA_ADDED_DATE);
  }

  public Integer getForMonth() {
    return this.getInteger(FOR_MONTH);
  }

  public Integer getForYear() {
    return this.getInteger(FOR_YEAR);
  }

  public List<String> getUsers() {
    Object usersObjects = this.get(USERS);
    if (usersObjects == null) {
      return null;
    }
    if (usersObjects instanceof java.sql.Array) {
      String[] result = new String[0];
      try {
        result = (String[]) ((java.sql.Array) usersObjects).getArray();
        List<String> users = new ArrayList<>();
        if (result.length == 0) {
          return users;
        }
        Collections.addAll(users, result);
        return users;
      } catch (SQLException e) {
        LOGGER.warn("Invalid users for CA '{}' in db ", this.getId(), e);
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")));
      }
    } else {
      LOGGER.warn("Not an instance of array");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")));
    }
  }

  public int getUsersCount() {
    return this.getInteger(USERS_COUNT);
  }

  public static LazyList<AJEntityClassContents> fetchAllContentsForStudent(String classId,
      int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED, classId, fromDate.toString(),
            toDate.toString(), userId).orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchAllContentsForTeacher(String classId,
      int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS, classId, forYear, forMonth)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForStudent(
      String classId, String contentType, int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED, classId, contentType,
            fromDate.toString(), toDate.toString(), userId)
        .orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForTeacher(
      String classId, String contentType, int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_GRP_BY_TYPE, classId, forYear, forMonth, contentType)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static void updateClassContentUsers(Long classContentId, String users, int count) {
    Base.exec(UPDATE_CLASS_CONTENTS_USERS, users, count, classContentId);
  }

  public static ValidatorRegistry getValidatorRegistry() {
    return new ClassContentsValidationRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
    return new ClassContentsConverterRegistry();
  }

  public void setInitialUsersCount() {
    this.setInteger(USERS_COUNT, -1);
  }
  
  public void setMasteryAccrualFlag(Boolean allowMasteryAccrual) {
      this.set(ALLOW_MASTERY_ACCRUAL, allowMasteryAccrual);
  }

  private static class ClassContentsValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return validatorRegistry.get(fieldName);
    }
  }

  private static class ClassContentsConverterRegistry implements ConverterRegistry {

    @Override
    public FieldConverter lookupConverter(String fieldName) {
      return converterRegistry.get(fieldName);
    }
  }
}
