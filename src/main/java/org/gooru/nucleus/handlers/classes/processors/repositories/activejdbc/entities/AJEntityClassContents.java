package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Table("class_contents")
@CompositePK({ "class_id", "content_id" })
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
    public static final String SEQUENCE = "sequence";
    public static final String DUE_DATE = "due_date";
    public static final String ASSESSMENT = "assessment";
    public static final String COLLECTION = "collection";
    public static final String RESOURCE = "resource";
    public static final String QUESTION = "question";
    public static final String ID_CONTENT = "contentId";

    public static final Set<String> CREATABLE_FIELDS = new HashSet<>(Arrays.asList(CLASS_ID, CTX_COURSE_ID, CTX_UNIT_ID,
        CTX_LESSON_ID, CTX_COLLECTION_ID, CONTENT_ID, CONTENT_TYPE, SEQUENCE, CREATED_AT, UPDATED_AT));
    public static final Set<String> ASSIGN_FIELDS = new HashSet<>(Arrays.asList(DUE_DATE, UPDATED_AT));
    public static final Set<String> MANDATORY_ASSIGN_FIELDS = new HashSet<>(Arrays.asList(DUE_DATE));
    private static final Set<String> MANDATORY_FIELDS = new HashSet<>(Arrays.asList(CONTENT_ID, CONTENT_TYPE));
    private static final Set<String> ACCEPT_CONTENT_TYPES = new HashSet<>(Arrays.asList(CONTENT_ID, CONTENT_TYPE));
    public static final List<String> RESPONSE_FIELDS =
        Arrays.asList(CONTENT_ID, CONTENT_TYPE, CTX_COURSE_ID, CTX_UNIT_ID, CTX_LESSON_ID, CTX_COLLECTION_ID, DUE_DATE);
    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    public static final String SELECT_CLASS_CONTENTS_TO_VALIDATE =
        "select class_id, content_id from class_contents where class_id = ?::uuid AND content_id = ?::uuid";

    public static final String SELECT_CLASS_CONTENT_MAX_SEQUENCEID =
        "SELECT max(sequence) FROM class_contents WHERE class_id = ?::uuid";

    public static final String SELECT_CLASS_CONTENTS =
        "select ctx_course_id, ctx_unit_id, ctx_lesson_id, ctx_collection_id, content_id, content_type, due_date from class_contents where class_id = ?::uuid and due_date is not null order by created_at desc";

    public static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE =
        "select ctx_course_id, ctx_unit_id, ctx_lesson_id, ctx_collection_id, content_id, content_type, due_date from class_contents where class_id = ?::uuid and content_type = ? order by created_at desc";

    public static final String FETCH_CLASS_CONTENT = "class_id = ?::uuid AND content_id = ?::uuid";

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
        converterMap.put(DUE_DATE,
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
        validatorMap.put(CONTENT_TYPE,
            (value -> FieldValidator.validateValueExists((String) value, ACCEPT_CONTENT_TYPES)));
        validatorMap.put(DUE_DATE,
            (value -> FieldValidator.validateDateWithFormatIfPresent(value, DateTimeFormatter.ISO_LOCAL_DATE, false)));
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

    public static FieldSelector assignFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(ASSIGN_FIELDS);
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(MANDATORY_ASSIGN_FIELDS);
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
