package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.ValidatorRegistry;
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
    public static final String CLASS_ID = "class_id";
    public static final String CTX_COURSE_ID = "ctx_course_id";
    public static final String CTX_UNIT_ID = "ctx_unit_id";
    public static final String CTX_LESSON_ID = "ctx_lesson_id";
    public static final String CTX_COLLECTION_ID = "ctx_collection_id";
    public static final String CONTENT_ID = "content_id";
    public static final String CONTENT_TYPE = "content_type";
    public static final String ACTIVATION_DATE = "activation_date";
    public static final String DCA_ADDED_DATE = "dca_added_date";
    public static final String ASSESSMENT = "assessment";
    public static final String COLLECTION = "collection";
    public static final String RESOURCE = "resource";
    public static final String QUESTION = "question";
    public static final String ID_CONTENT = "contentId";
    private static final String SORT_DESC = " desc";
    public static final String ID = "id";
    private static final String COMMA_SEPARATOR = ",";

    public static final Set<String> CREATABLE_FIELDS = new HashSet<>(Arrays
        .asList(ID, CLASS_ID, CTX_COURSE_ID, CTX_UNIT_ID, CTX_LESSON_ID, CTX_COLLECTION_ID, CONTENT_ID, CONTENT_TYPE,
            CREATED_AT, UPDATED_AT, DCA_ADDED_DATE));
    public static final Set<String> UPDATEABLE_FIELDS = new HashSet<>(Arrays.asList(ACTIVATION_DATE, UPDATED_AT));
    private static final Set<String> MANDATORY_FIELDS =
        new HashSet<>(Arrays.asList(CONTENT_ID, CONTENT_TYPE, DCA_ADDED_DATE));
    private static final Set<String> ACCEPT_CONTENT_TYPES =
        new HashSet<>(Arrays.asList(ASSESSMENT, COLLECTION, RESOURCE, QUESTION));
    public static final List<String> RESPONSE_FIELDS = Arrays
        .asList(ID, CONTENT_ID, CONTENT_TYPE, CTX_COURSE_ID, CTX_UNIT_ID, CTX_LESSON_ID, CTX_COLLECTION_ID,
            DCA_ADDED_DATE, ACTIVATION_DATE, CREATED_AT);
    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    public static final String SELECT_CLASS_CONTENTS_TO_VALIDATE =
        "select class_id, content_id from class_contents where class_id = ?::uuid AND content_id = ?::uuid AND "
            + "activation_date = ?::date";

    public static final String SELECT_DUPLICATED_ADDED_CONTENT =
        "class_id = ?::uuid and content_id = ?::uuid and content_type = ? and dca_added_date::DATE = ?::DATE";

    private static final String SELECT_CLASS_CONTENTS =
        "class_id = ?::uuid AND dca_added_date BETWEEN ?::date AND ?::date";

    private static final String SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED =
        "class_id = ?::uuid AND activation_date BETWEEN ?::date AND ?::date";

    private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE =
        "class_id = ?::uuid AND content_type = ? AND dca_added_date BETWEEN ?::date AND ?::date";

    private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED =
        "class_id = ?::uuid AND content_type = ? AND activation_date BETWEEN ?::date AND ?::date";

    public static final String FETCH_CLASS_CONTENT = "id = ?::bigint AND class_id = ?::uuid";

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(CLASS_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CTX_COURSE_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CTX_UNIT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CTX_LESSON_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CTX_COLLECTION_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CONTENT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(ACTIVATION_DATE,
            (fieldValue -> FieldConverter.convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
        converterMap.put(DCA_ADDED_DATE,
            (fieldValue -> FieldConverter.convertFieldToDateWithFormat(fieldValue, DateTimeFormatter.ISO_LOCAL_DATE)));
        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(CLASS_ID, (FieldValidator::validateUuid));
        validatorMap.put(CONTENT_ID, (FieldValidator::validateUuid));
        validatorMap.put(CTX_COURSE_ID, (value -> FieldValidator.validateUuidIfPresent((String) value)));
        validatorMap.put(CTX_UNIT_ID, (value -> FieldValidator.validateUuidIfPresent((String) value)));
        validatorMap.put(CTX_LESSON_ID, (value -> FieldValidator.validateUuidIfPresent((String) value)));
        validatorMap.put(CTX_COLLECTION_ID, (value -> FieldValidator.validateUuidIfPresent((String) value)));
        validatorMap
            .put(CONTENT_TYPE, (value -> FieldValidator.validateValueExists((String) value, ACCEPT_CONTENT_TYPES)));
        validatorMap.put(DCA_ADDED_DATE, (value -> FieldValidator
            .validateDateWithFormatWithInDaysBoundary(value, DateTimeFormatter.ISO_LOCAL_DATE, 1, 1)));
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

    public static FieldSelector updateFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(UPDATEABLE_FIELDS);
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

    public void setDefaultActivationDateIfNotPresent() {
        if (this.getDate(ACTIVATION_DATE) == null) {
            this.set(ACTIVATION_DATE,
                FieldConverter.convertFieldToDateWithFormat(LocalDate.now(), DateTimeFormatter.ISO_LOCAL_DATE));
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

    public String getCtxCourseId() {
        return this.getString(CTX_COURSE_ID);
    }

    public String getCtxUnitId() {
        return this.getString(CTX_UNIT_ID);
    }

    public String getCtxLessonId() {
        return this.getString(CTX_LESSON_ID);
    }

    public String getCtxCollectionId() {
        return this.getString(CTX_COLLECTION_ID);
    }

    public static String getClassContent(boolean isStudent) {
        if (isStudent) {
            return SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED;
        }
        return SELECT_CLASS_CONTENTS;
    }

    public static String getClassContentWithGrouping(boolean isStudent) {
        if (isStudent) {
            return SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED;
        }
        return SELECT_CLASS_CONTENTS_GRP_BY_TYPE;
    }

    public static String getSequenceFieldNameWithSortOrder(boolean isStudent) {
        if (isStudent) {
            return ACTIVATION_DATE + SORT_DESC + COMMA_SEPARATOR + ID + SORT_DESC;
        } else {
            return DCA_ADDED_DATE + SORT_DESC + COMMA_SEPARATOR + CREATED_AT + SORT_DESC;
        }
    }

    public Date getCreatedDate() {
        return this.getDate(CREATED_AT);
    }

    public Date getDcaAddedDate() {
        return this.getDate(DCA_ADDED_DATE);
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new ClassContentsValidationRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new ClassContentsConverterRegistry();
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