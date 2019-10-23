package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonObject;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;


/**
 * Created by ashish on 8/2/16.
 */
@Table("class")
public class AJEntityClass extends Model {

  public static final String ID = "id";
  public static final String CREATOR_ID = "creator_id";
  public static final String IS_DELETED = "is_deleted";
  private static final String MODIFIER_ID = "modifier_id";
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "description";
  private static final String GREETING = "greeting";
  public static final String CLASS_SHARING = "class_sharing";
  private static final String COVER_IMAGE = "cover_image";
  private static final String GRADE = "grade";
  public static final String CODE = "code";
  private static final String MIN_SCORE = "min_score";
  private static final String END_DATE = "end_date";
  private static final String GOORU_VERSION = "gooru_version";
  public static final String CONTENT_VISIBILITY = "content_visibility";
  private static final String IS_ARCHIVED = "is_archived";
  private static final String IS_PUBLISHED = "is_published";
  public static final String COLLABORATOR = "collaborator";
  public static final String COURSE_ID = "course_id";
  private static final String CREATED_AT = "created_at";
  private static final String UPDATED_AT = "updated_at";
  private static final String CREATOR_SYSTEM = "creator_system";
  private static final String ROSTER_ID = "roster_id";
  private static final int CURRENT_VERSION = 3;
  public static final String INVITEES = "invitees";
  private static final String TENANT = "tenant";
  private static final String TENANT_ROOT = "tenant_root";
  private static final String GRADE_UPPER_BOUND = "grade_upper_bound";
  private static final String GRADE_LOWER_BOUND = "grade_lower_bound";
  private static final String GRADE_CURRENT = "grade_current";
  private static final String ROUTE0 = "route0_applicable";
  public static final String SETTING = "setting";
  public static final String PREFERENCE = "preference";
  public static final String COURSE_PREMIUM = "course.premium";
  public static final String OWNER_ID = "owner_id";
  public static final String PRIMARY_LANGUAGE = "primary_language";
  private static final String FORCE_CALCULATE_ILP = "force_calculate_ilp";
  private static final String MILESTONE_VIEW_APPLICABLE = "milestone_view_applicable";

  public static final String STUDENTS = "students";
  
  public static final String SECONDARY_CLASSES = "secondary.classes";
  public static final String SECONDARY_CLASSES_LIST = "list";
  public static final String SECONDARY_CLASSES_CONFIRMATION = "confirmation";
  public static final String CLASS_DEFAULT_VIEW = "class.default.view";
  public static final String CONTENT_ADD_DEFAULT_VIEW = "content.add.default.view";
  public static final String MASTERY_APPLICABLE = "mastery.applicable";

  // Dummy field names for Content Visibility
  // TODO this needs to change when going through the setting of content visibility in new model
  public static final String CV_COLLECTIONS = "collections";
  public static final String CV_ASSESSMENTS = "assessments";
  private static final Set<String> CV_FIELDS = new HashSet<>(
      Arrays.asList(CV_ASSESSMENTS, CV_COLLECTIONS));

  private static final String CONTENT_VISIBILITY_TYPE_NAME = "content_visibility_type";
  private static final String CONTENT_VISIBILITY_TYPE_VISIBLE_NONE = "visible_none";
  public static final String CONTENT_VISIBILITY_TYPE_VISIBLE_COLLECTION = "visible_collections";
  public static final String CONTENT_VISIBILITY_TYPE_VISIBLE_ALL = "visible_all";
  private static final String CLASS_SHARING_TYPE_NAME = "class_sharing_type";
  public static final String CLASS_SHARING_TYPE_OPEN = "open";
  private static final String CLASS_SHARING_TYPE_RESTRICTED = "restricted";

  public static final String FETCH_QUERY_FILTER = "id = ?::uuid and is_deleted = false";
  public static final String FETCH_MULTIPLE_QUERY_FILTER = "id = ANY(?::uuid[]) and is_deleted = false";
  public static final String FETCH_MULTIPLE_NON_DELETED_NON_ARCHIVED  = "id = ANY(?::uuid[]) and is_deleted = false and is_archived = false";
  public static final String FETCH_FOR_OWNER_COLLABORATOR_QUERY =
      "select id, creator_id from class where (creator_id = ?::uuid or collaborator ?? ? ) and is_deleted = false "
          + "order by created_at desc";
  public static final String FETCH_FOR_COURSE_QUERY_FILTER = "course_id = ?::uuid and is_deleted = false";
  public static final String FETCH_VIA_CODE_FILTER = "code = ? and is_deleted = false";
  public static final String DELETE_QUERY =
      "select id, creator_id, end_date, course_id, gooru_version, is_archived from class where id = ?::uuid and "
          + "is_deleted = false";
  public static final String ARCHIVE_QUERY =
      "select id, creator_id, end_date, course_id, gooru_version, is_archived from class where id = ?::uuid and "
          + "is_deleted = false";
  public static final String CODE_UNIQUENESS_QUERY = "code = ?";
  
  public static final String FIND_SECONDARY_CLASSES =
      "SELECT id, title, code, preference FROM class WHERE (creator_id = ?::uuid OR collaborator ?? ?) AND is_deleted = false AND"
      + " is_archived = false AND preference IS NOT NULL AND id <> ?::uuid";

  private static final Set<String> EDITABLE_FIELDS = new HashSet<>(Arrays
      .asList(TITLE, DESCRIPTION, GREETING, GRADE, CLASS_SHARING, COVER_IMAGE, MIN_SCORE, END_DATE,
          COLLABORATOR, SETTING));
  private static final Set<String> CREATABLE_FIELDS = new HashSet<>(Arrays
      .asList(TITLE, DESCRIPTION, GREETING, GRADE, CLASS_SHARING, COVER_IMAGE, MIN_SCORE, END_DATE,
          COLLABORATOR, CONTENT_VISIBILITY, CREATOR_SYSTEM, ROSTER_ID, SETTING,
          FORCE_CALCULATE_ILP));
  private static final Set<String> MANDATORY_FIELDS = new HashSet<>(
      Arrays.asList(TITLE, CLASS_SHARING));
  public static final Set<String> FORBIDDEN_FIELDS = new HashSet<>(
      Arrays.asList(ID, CREATED_AT, UPDATED_AT, CREATOR_ID, MODIFIER_ID, IS_DELETED, GOORU_VERSION,
          IS_ARCHIVED));
  private static final Set<String> COLLABORATOR_FIELDS = new HashSet<>(Arrays.asList(COLLABORATOR));
  private static final Set<String> INVITE_MANDATORY_FIELDS = new HashSet<>(Arrays.asList(INVITEES));
  private static final Set<String> INVITE_ALLOWED_FIELDS = new HashSet<>(
      Arrays.asList(INVITEES, CREATOR_SYSTEM));
  public static final List<String> FETCH_QUERY_FIELD_LIST = Arrays
      .asList(ID, CREATOR_ID, TITLE, DESCRIPTION, GREETING, GRADE, CLASS_SHARING, COVER_IMAGE, CODE,
          MIN_SCORE, END_DATE, COURSE_ID, COLLABORATOR, GOORU_VERSION, CONTENT_VISIBILITY,
          IS_ARCHIVED, SETTING, ROSTER_ID, CREATED_AT, UPDATED_AT, GRADE_CURRENT, GRADE_LOWER_BOUND,
          GRADE_UPPER_BOUND, ROUTE0, FORCE_CALCULATE_ILP, PREFERENCE, PRIMARY_LANGUAGE,
          MILESTONE_VIEW_APPLICABLE);
  public static final List<String> SECONDARY_CLASSES_FIELD_LIST = Arrays.asList(ID, TITLE, CODE);
  
  private static final Set<String> JOIN_CLASS_FIELDS = new HashSet<>(
      Arrays.asList(ROSTER_ID, CREATOR_SYSTEM));
  
  private static final Set<String> ADD_STUDENTS_FIELDS = new HashSet<>(Arrays.asList(STUDENTS));

  private static final Set<String> CLASS_PREFERENCE_FIELDS =
      new HashSet<>(Arrays.asList(PREFERENCE));

  private static final Map<String, FieldValidator> validatorRegistry;
  private static final Map<String, FieldConverter> converterRegistry;

  static {
    validatorRegistry = initializeValidators();
    converterRegistry = initializeConverters();
  }

  private static Map<String, FieldConverter> initializeConverters() {
    Map<String, FieldConverter> converterMap = new HashMap<>();
    converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(GRADE, (FieldConverter::convertFieldToJson));
    converterMap.put(END_DATE,
        (fieldValue -> FieldConverter
            .convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
    converterMap.put(CONTENT_VISIBILITY,
        fieldValue -> FieldConverter
            .convertFieldToNamedType(fieldValue, CONTENT_VISIBILITY_TYPE_NAME));
    converterMap
        .put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap
        .put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap
        .put(COURSE_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(CLASS_SHARING,
        (fieldValue -> FieldConverter
            .convertFieldToNamedType(fieldValue, CLASS_SHARING_TYPE_NAME)));
    converterMap.put(COLLABORATOR, (FieldConverter::convertFieldToJson));
    converterMap
        .put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap
        .put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
    converterMap.put(PREFERENCE, (FieldConverter::convertFieldToJson));
    return Collections.unmodifiableMap(converterMap);
  }

  private static Map<String, FieldValidator> initializeValidators() {
    Map<String, FieldValidator> validatorMap = new HashMap<>();
    validatorMap.put(ID, (FieldValidator::validateUuid));
    validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
    validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringIfPresent(value, 5000));
    validatorMap.put(GREETING, (value) -> FieldValidator.validateStringIfPresent(value, 5000));
    validatorMap.put(GRADE, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(CLASS_SHARING, (value) -> ((value instanceof String) && (
        CLASS_SHARING_TYPE_OPEN.equalsIgnoreCase((String) value) || CLASS_SHARING_TYPE_RESTRICTED
            .equalsIgnoreCase((String) value))));
    validatorMap.put(COVER_IMAGE, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
    validatorMap.put(MIN_SCORE, (FieldValidator::validateInteger));
    validatorMap.put(END_DATE,
        (value -> FieldValidator
            .validateDateWithFormat(value, DateTimeFormatter.ISO_LOCAL_DATE, false, false)));
    validatorMap.put(COURSE_ID, (value -> FieldValidator.validateUuidIfPresent((String) value)));
    validatorMap.put(COLLABORATOR,
        (value) -> FieldValidator
            .validateDeepJsonArrayIfPresent(value, FieldValidator::validateUuid));
    validatorMap.put(CREATOR_SYSTEM, (value) -> FieldValidator.validateStringIfPresent(value, 255));
    validatorMap.put(ROSTER_ID, (value) -> FieldValidator.validateStringIfPresent(value, 512));
    validatorMap.put(INVITEES,
        (value) -> FieldValidator
            .validateDeepJsonArrayIfPresent(value, FieldValidator::validateEmail));
    validatorMap.put(CV_ASSESSMENTS, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(CV_COLLECTIONS, FieldValidator::validateJsonArrayIfPresent);
    validatorMap.put(STUDENTS, FieldValidator::validateJsonArray);
    validatorMap.put(TENANT, (FieldValidator::validateUuid));
    validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
    validatorMap.put(PREFERENCE, (FieldValidator::validateJsonIfPresent));
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

  public static FieldSelector inviteStudentFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> mandatoryFields() {
        return Collections.unmodifiableSet(INVITE_MANDATORY_FIELDS);
      }

      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(INVITE_ALLOWED_FIELDS);
      }
    };
  }

  public static FieldSelector joinClassFieldSelector() {
    return () -> Collections.unmodifiableSet(JOIN_CLASS_FIELDS);
  }
  
  public static FieldSelector addStudentsToClassFieldSelector() {
    return () -> Collections.unmodifiableSet(ADD_STUDENTS_FIELDS);
  }

  public static FieldSelector updateClassFieldSelector() {
    return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
  }

  public static FieldSelector updateClassPreferenceFieldSelector() {
    return () -> Collections.unmodifiableSet(CLASS_PREFERENCE_FIELDS);
  }

  public static FieldSelector updateCollaboratorFieldSelector() {
    return new FieldSelector() {
      @Override
      public Set<String> mandatoryFields() {
        return Collections.emptySet();
      }

      @Override
      public Set<String> allowedFields() {
        return Collections.unmodifiableSet(COLLABORATOR_FIELDS);
      }
    };
  }

  public static FieldSelector contentVisibilityFieldSelector() {
    return () -> Collections.unmodifiableSet(CV_FIELDS);
  }

  public static ValidatorRegistry getValidatorRegistry() {
    return new ClassValidationRegistry();
  }

  public static ConverterRegistry getConverterRegistry() {
    return new ClassConverterRegistry();
  }

  public void setContentVisibility(String visibility) {
    setFieldUsingConverter(CONTENT_VISIBILITY, visibility);
  }

  private static final String DEFAULT_CONTENT_VISIBILITY = CONTENT_VISIBILITY_TYPE_VISIBLE_COLLECTION;
  private static final String DEFAULT_ALTERNATE_CONTENT_VISIBILITY = CONTENT_VISIBILITY_TYPE_VISIBLE_ALL;

  public String getDefaultAlternateContentVisibility() {
    return DEFAULT_ALTERNATE_CONTENT_VISIBILITY;
  }

  public String getDefaultContentVisibility() {
    return DEFAULT_CONTENT_VISIBILITY;
  }

  public String getContentVisibility() {
    // Treat null and default as visible collections
    String contentVisibilitySetting = this.getString(CONTENT_VISIBILITY);
    final String setting = this.getString(SETTING);
    final JsonObject classSetting =
        setting != null ? new JsonObject(this.getString(SETTING)) : null;
    if (classSetting != null && classSetting.containsKey(COURSE_PREMIUM) && classSetting
        .getBoolean(COURSE_PREMIUM)) {
      return AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_ALL;
    } else if (contentVisibilitySetting == null) {
      return DEFAULT_CONTENT_VISIBILITY;
    } else if (contentVisibilitySetting
        .equalsIgnoreCase(AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_ALL)) {
      return AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_ALL;
    } else if (contentVisibilitySetting
        .equalsIgnoreCase(AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_COLLECTION)) {
      return AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_COLLECTION;
    } else if (contentVisibilitySetting
        .equalsIgnoreCase(AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_NONE)) {
      return AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_NONE;
    } else {
      return DEFAULT_CONTENT_VISIBILITY;
    }
  }

  public void setModifierId(String modifier) {
    setFieldUsingConverter(MODIFIER_ID, modifier);
  }

  public void setCreatorId(String creator) {
    setFieldUsingConverter(CREATOR_ID, creator);
  }

  public void setCourseId(String courseId) {
    setFieldUsingConverter(COURSE_ID, courseId);
  }

  public void setIdWithConverter(String id) {
    setFieldUsingConverter(ID, id);
  }

  public boolean isCurrentVersion() {
    return getInteger(GOORU_VERSION) == CURRENT_VERSION;
  }

  public boolean isArchived() {
    return getBoolean(IS_ARCHIVED);
  }

  public void setIsArchived(boolean isArchived) {
    this.setBoolean(IS_ARCHIVED, isArchived);
  }

  public String getCourseId() {
    return this.getString(COURSE_ID);
  }

  public void setVersion() {
    this.set(GOORU_VERSION, CURRENT_VERSION);
  }

  public void setPrimaryLanguage(Integer languageId) {
    this.setInteger(PRIMARY_LANGUAGE, languageId);
  }

  public boolean isPublished() {
    return getBoolean(IS_PUBLISHED);
  }

  public boolean isForceCalculateILP() {
    return getBoolean(FORCE_CALCULATE_ILP);
  }

  public void setForceCalculateIlp(boolean forceCalculateIlp) {
    this.setBoolean(FORCE_CALCULATE_ILP, forceCalculateIlp);
  }

  public void adjustEndDate(String defaultEndDate) {
    java.sql.Date payloadDate = this.getDate(END_DATE);
    if (payloadDate == null) {
      setEndDate(defaultEndDate);
      return;
    }
    java.sql.Date defaultDate = java.sql.Date.valueOf(defaultEndDate);
    if (payloadDate.before(defaultDate)) {
      // payload date is acceptable
      return;
    }
    setEndDate(defaultEndDate);
  }

  public void setTenant(String tenant) {
    setFieldUsingConverter(TENANT, tenant);
  }

  public void setGradeUpperBound(Long upperBound) {
    this.setLong(GRADE_UPPER_BOUND, upperBound);
  }

  public void setGradeLowerBound(Long lowerBound) {
    this.setLong(GRADE_LOWER_BOUND, lowerBound);
  }

  public void setGradeCurrent(Long gradeCurrent) {
    this.setLong(GRADE_CURRENT, gradeCurrent);
  }

  public void setTenantRoot(String tenantRoot) {
    setFieldUsingConverter(TENANT_ROOT, tenantRoot);
  }

  public void setPreference(String preference) {
    setFieldUsingConverter(PREFERENCE, preference);
  }

  public String getTenant() {
    return this.getString(TENANT);
  }

  public String getTenantRoot() {
    return this.getString(TENANT_ROOT);
  }

  public Long getGradeUpperBound() {
    return this.getLong(GRADE_UPPER_BOUND);
  }

  public Long getGradeLowerBound() {
    return this.getLong(GRADE_LOWER_BOUND);
  }

  public Long getGradeCurrent() {
    return this.getLong(GRADE_CURRENT);
  }

  public void setEndDate(String classEndDate) {
    FieldConverter fc = converterRegistry.get(END_DATE);
    if (fc != null) {
      this.set(END_DATE, fc.convertField(classEndDate));
    }
  }

  public void setClassSettings(JsonObject classSettings) {
    this.set(SETTING, FieldConverter.convertFieldToJson(classSettings));
  }

  public void setClassPreference(JsonObject classPreference) {
    this.set(PREFERENCE, FieldConverter.convertFieldToJson(classPreference));
  }

  public void setMilestoneViewApplicable(Boolean applicability) {
    this.set(MILESTONE_VIEW_APPLICABLE, applicability);
  }

  public Boolean getMilestoneViewApplicable() {
    return this.getBoolean(MILESTONE_VIEW_APPLICABLE);
  }

  private void setFieldUsingConverter(String fieldName, Object fieldValue) {
    FieldConverter fc = converterRegistry.get(fieldName);
    if (fc != null) {
      this.set(fieldName, fc.convertField(fieldValue));
    } else {
      this.set(fieldName, fieldValue);
    }
  }

  public void setRoute0(boolean route0) {
    this.setBoolean(ROUTE0, route0);
  }

  private static class ClassValidationRegistry implements ValidatorRegistry {

    @Override
    public FieldValidator lookupValidator(String fieldName) {
      return validatorRegistry.get(fieldName);
    }
  }

  private static class ClassConverterRegistry implements ConverterRegistry {

    @Override
    public FieldConverter lookupConverter(String fieldName) {
      return converterRegistry.get(fieldName);
    }
  }

}
