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
  public static final String OFFLINE_ACTIVITY = "offline-activity";
  public static final String RESOURCE = "resource";
  public static final String QUESTION = "question";
  public static final String ID_CONTENT = "contentId";
  public static final String USERS = "users";
  public static final String USERS_COUNT = "users_count";
  public static final String END_DATE = "end_date";
  private static final String IS_COMPLETED = "is_completed";
  public static final String ID = "id";
  public static final Pattern ASSESSMENT_TYPES = Pattern.compile("assessment|assessment-external");
  public static final Pattern COLLECTION_TYPES = Pattern.compile("collection|collection-external");
  public static final String MEETING_ID = "meeting_id";
  public static final String MEETING_URL = "meeting_url";
  public static final String MEETING_START_TIME = "meeting_starttime";
  public static final String MEETING_END_TIME = "meeting_endtime";
  public static final String MEETING_TIME_ZONE = "meeting_timezone";


  private static final Set<String> CREATABLE_FIELDS =
      new HashSet<>(Arrays.asList(ID, CLASS_ID, FOR_MONTH, FOR_YEAR, CONTENT_ID, CONTENT_TYPE,
          CREATED_AT, UPDATED_AT, DCA_ADDED_DATE, END_DATE));
  private static final Set<String> UPDATE_USERS_FIELDS = new HashSet<>(Arrays.asList(USERS));
  private static final Set<String> UPDATEABLE_FIELDS =
      new HashSet<>(Arrays.asList(ACTIVATION_DATE, DCA_ADDED_DATE, UPDATED_AT));
  private static final Set<String> UPDATE_MASTERY_ACCRUAL_FIELDS =
      new HashSet<>(Arrays.asList(ALLOW_MASTERY_ACCRUAL));
  private static final Set<String> MANDATORY_FIELDS =
      new HashSet<>(Arrays.asList(CONTENT_ID, CONTENT_TYPE, FOR_MONTH, FOR_YEAR));
  public static final Set<String> ACCEPT_CONTENT_TYPES = new HashSet<>(Arrays.asList(ASSESSMENT,
      COLLECTION, ASSESSMENT_EXTERNAL, COLLECTION_EXTERNAL, RESOURCE, QUESTION, OFFLINE_ACTIVITY));
  public static final List<String> RESPONSE_FIELDS_FOR_TEACHER = Arrays.asList(ID, CONTENT_ID,
      CONTENT_TYPE, FOR_YEAR, FOR_MONTH, DCA_ADDED_DATE, ACTIVATION_DATE, CREATED_AT, USERS_COUNT,
      ALLOW_MASTERY_ACCRUAL, IS_COMPLETED, END_DATE, CLASS_ID, MEETING_ID, MEETING_URL,
      MEETING_START_TIME, MEETING_END_TIME, MEETING_TIME_ZONE);
  public static final List<String> RESPONSE_FIELDS_FOR_STUDENT =
      Arrays.asList(ID, CONTENT_ID, CONTENT_TYPE, FOR_YEAR, FOR_MONTH, DCA_ADDED_DATE,
          ACTIVATION_DATE, CREATED_AT, ALLOW_MASTERY_ACCRUAL, IS_COMPLETED, END_DATE, MEETING_URL,
          MEETING_START_TIME, MEETING_END_TIME, MEETING_TIME_ZONE);
  private static final Set<String> MEETING_SETUP_FIELDS =
      new HashSet<>(Arrays.asList(MEETING_ID, MEETING_URL, MEETING_START_TIME, MEETING_END_TIME, MEETING_TIME_ZONE));
  private static final Map<String, FieldValidator> validatorRegistry;
  private static final Map<String, FieldConverter> converterRegistry;

  public static final String SELECT_CLASS_CONTENTS_TO_VALIDATE_ACTIVATION =
      "select class_id, content_id from class_contents where class_id = ?::uuid AND content_id = ?::uuid AND "
          + "activation_date = ?::date";

  public static final String SELECT_CLASS_CONTENTS_TO_VALIDATE_SCHEDULE =
      "select class_id, content_id from class_contents where class_id = ?::uuid AND content_id = ?::uuid AND "
          + "dca_added_date = ?::date";

  public static final String SELECT_DUPLICATED_ADDED_CONTENT =
      "class_id = ?::uuid and content_id = ?::uuid and content_type = ? and dca_added_date::DATE = ?::DATE";

  public static final String SELECT_DUPLICATED_ADDED_CONTENT_FOR_MONTH =
      "class_id = ?::uuid and content_id = ?::uuid and content_type = ? and dca_added_date::DATE is null "
          + " and for_month = ? and for_year = ?";

  private static final String UPDATE_CLASS_CONTENTS_USERS =
      "update class_contents set users = ?::text[], users_count = ? where id = ?";

  public static final String FETCH_CLASS_CONTENT = "id = ?::bigint AND class_id = ?::uuid";

  static {
    validatorRegistry = initializeValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap.put(CLASS_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(CONTENT_ID,
        (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(ACTIVATION_DATE, (fieldValue -> FieldConverter
        .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    converterMap.put(DCA_ADDED_DATE, (fieldValue -> FieldConverter
        .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    converterMap.put(END_DATE, (fieldValue -> FieldConverter
        .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    return Collections.unmodifiableMap(converterMap);
  }
  

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(CLASS_ID, (FieldValidator::validateUuid));
    validatorMap.put(CONTENT_ID, (FieldValidator::validateUuid));
    validatorMap.put(FOR_MONTH, FieldValidator::validateMonth);
    validatorMap.put(FOR_YEAR, FieldValidator::validateYear);
    validatorMap.put(CONTENT_TYPE,
        (value -> FieldValidator.validateValueExists((String) value, ACCEPT_CONTENT_TYPES)));
    validatorMap.put(DCA_ADDED_DATE, (value -> FieldValidator
        .validateDateWithFormatWithInDaysBoundary(value, DateTimeFormatter.ISO_LOCAL_DATE, 1)));
    validatorMap.put(USERS, (value) -> FieldValidator
        .validateDeepJsonArrayIfPresentAllowEmpty(value, FieldValidator::validateUuid));
    validatorMap.put(ALLOW_MASTERY_ACCRUAL, FieldValidator::validateBooleanIfPresent);
    validatorMap.put(MEETING_START_TIME, (value -> FieldValidator
        .validateDateTimeWithFormatWithInDaysBoundary(value, DateTimeFormatter.ISO_DATE_TIME, 1)));
    validatorMap.put(MEETING_END_TIME, (value -> FieldValidator
        .validateDateTimeWithFormatWithInDaysBoundary(value, DateTimeFormatter.ISO_DATE_TIME, 1)));
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

  public static FieldSelector updateMasteryAccrualFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(UPDATE_MASTERY_ACCRUAL_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(UPDATE_MASTERY_ACCRUAL_FIELDS);
      }
    };
  }

  public static FieldSelector meetingSetupFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(MEETING_SETUP_FIELDS);
      }

      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(MEETING_SETUP_FIELDS);
      }
    };
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

  public String getClassId() {
    return this.getString(CLASS_ID);
  }

  public void setActivationDateIfNotPresent(LocalDate activationDate) {
    if (this.getDate(ACTIVATION_DATE) == null) {
      this.set(ACTIVATION_DATE, FieldConverter.convertFieldToDateWithFormat(activationDate,
          DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }

  public void setDcaAddedDateIfNotPresent(LocalDate dcaAddedDate) {
    if (this.getDate(DCA_ADDED_DATE) == null) {
      this.set(DCA_ADDED_DATE, FieldConverter.convertFieldToDateWithFormat(dcaAddedDate,
          DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }

  public void setEndDateIfNotPresent(LocalDate endDate) {
    if (this.getDate(END_DATE) == null) {
      this.set(END_DATE,
          FieldConverter.convertFieldToDateWithFormat(endDate, DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }

  public void setCompleted() {
    this.setBoolean(IS_COMPLETED, true);
  }

  public boolean isActivityOffline() {
    String activityType = this.getString(CONTENT_TYPE);
    return OFFLINE_ACTIVITY.equals(activityType);
  }

  public boolean isOfflineActivityActive() {
    // Activity should have been started : activation date is today or in past
    // It is not completed yet
    if (isActivityOffline() && this.getActivationDate() != null) {
      LocalDate startDate = this.getActivationDate().toLocalDate();
      return !startDate.isAfter(LocalDate.now()) && !this.isCompleted();
    }
    return false;
  }

  public Long getDcaId() {
    return this.getLong("id");
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

  public Date getEndDate() {
    return this.getDate(END_DATE);
  }

  public Boolean isCompleted() {
    return this.getBoolean(IS_COMPLETED);
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
