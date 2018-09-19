package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.*;

import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 8/2/16.
 */
class FetchClassesForUserHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchClassesForUserHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private final List<String> classIdList = new ArrayList<>();
    private JsonArray memberClassIdArray;
    private static final String RESPONSE_BUCKET_OWNER = "owner";
    private static final String RESPONSE_BUCKET_COLLABORATOR = "collaborator";
    private static final String RESPONSE_BUCKET_MEMBER = "member";
    private static final String RESPONSE_BUCKET_CLASSES = "classes";
    private static final String RESPONSE_BUCKET_MEMBER_COUNT = "member_count";
    private static final String RESPONSE_BUCKET_TEACHER_DETAILS = "teacher_details";

    FetchClassesForUserHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        if ((context.userId() == null) || context.userId().isEmpty()
            || MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(context.userId())) {
            LOGGER.warn("Invalid user");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        // Nothing to validate
        return AuthorizerBuilder.buildFetchClassesForUserAuthorizer(context).authorize(null);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject result = new JsonObject();
        ExecutionResult<MessageResponse> response = populateOwnedOrCollaboratedClassesId(result);
        if (response.hasFailed()) {
            return response;
        }
        response = populateMembershipClassesId(result);
        if (response.hasFailed()) {
            return response;
        }
        response = populateClassMemberCounts(result);
        if (response.hasFailed()) {
            return response;
        }
        response = populateClassDetails(result);
        if (response.hasFailed()) {
            return response;
        }
        return populateTeacherDetails(result);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

    private ExecutionResult<MessageResponse> populateOwnedOrCollaboratedClassesId(JsonObject result) {
        try {
            LazyList<AJEntityClass> classes = AJEntityClass.findBySQL(AJEntityClass.FETCH_FOR_OWNER_COLLABORATOR_QUERY,
                context.userId(), context.userId());
            JsonArray ownedClassIds = new JsonArray();
            JsonArray collaboratedClassIds = new JsonArray();
            for (AJEntityClass entityClass : classes) {
                String classId = entityClass.getId().toString();
                String creatorId = entityClass.getString(AJEntityClass.CREATOR_ID);
                if (context.userId().equalsIgnoreCase(creatorId)) {
                    ownedClassIds.add(classId);
                } else {
                    collaboratedClassIds.add(classId);
                }
                classIdList.add(classId);
            }
            result.put(RESPONSE_BUCKET_OWNER, ownedClassIds);
            result.put(RESPONSE_BUCKET_COLLABORATOR, collaboratedClassIds);
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        } catch (DBException dbe) {
            LOGGER.warn("Unable to fetch owned or collaborated classes for user '{}'", context.userId(), dbe);
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

    private ExecutionResult<MessageResponse> populateMembershipClassesId(JsonObject result) {
        try {
            memberClassIdArray = new JsonArray();
            List memberClassIdList = Base.firstColumn(AJClassMember.FETCH_USER_MEMBERSHIP_QUERY, context.userId());
            for (Object member : memberClassIdList) {
                String classId = member.toString();
                memberClassIdArray.add(classId);
                classIdList.add(classId);
            }
            result.put(RESPONSE_BUCKET_MEMBER, memberClassIdArray);
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        } catch (DBException dbe) {
            LOGGER.warn("Unable to fetch membership classes for user '{}'", context.userId(), dbe);
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

    private ExecutionResult<MessageResponse> populateClassMemberCounts(JsonObject result) {
        try {
            JsonObject memberCount = new JsonObject();
            /*
            List<Map> rs = Base.findAll(AJClassMember.FETCH_MEMBERSHIP_COUNT_FOR_CLASSES,
                Utils.convertListToPostgresArrayStringRepresentation(classIdList));
            rs.forEach(map -> memberCount.put(map.get("class_id").toString(), map.get("count")));
            */
            result.put(RESPONSE_BUCKET_MEMBER_COUNT, memberCount);
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        } catch (DBException dbe) {
            LOGGER.warn("Unable to fetch membership classes for user '{}'", context.userId(), dbe);
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }

    private ExecutionResult<MessageResponse> populateClassDetails(JsonObject result) {
        LazyList<AJEntityClass> classes = AJEntityClass.where(AJEntityClass.FETCH_MULTIPLE_QUERY_FILTER,
            Utils.convertListToPostgresArrayStringRepresentation(classIdList));
        List<String> courseIdList = fetchCourseIdsForClasses(classes);

        Map<String, String> courseTitleMap = new HashMap<>(classes.size());
        Map<String, String> courseVersionMap = new HashMap<>((classes.size()));

        populateCourseTitleVersionForIds(courseIdList, courseTitleMap, courseVersionMap);

        JsonArray classDetails = createClassDetailsResponse(classes, courseTitleMap, courseVersionMap);

        result.put(RESPONSE_BUCKET_CLASSES, classDetails);
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    private static JsonArray createClassDetailsResponse(LazyList<AJEntityClass> classes,
        Map<String, String> courseTitleMap, Map<String, String> courseVersionMap) {
        JsonArray classDetails = new JsonArray();

        classes.forEach(classEntry -> {
            JsonObject classJson = new JsonObject(
                JsonFormatterBuilder.buildSimpleJsonFormatter(false, AJEntityClass.FETCH_QUERY_FIELD_LIST)
                    .toJson(classEntry));
            String courseTitle = courseTitleMap.getOrDefault(classEntry.getString(AJEntityClass.COURSE_ID), null);
            String courseVersion = courseVersionMap.getOrDefault(classEntry.getString(AJEntityClass.COURSE_ID), null);
            classJson.put(AJEntityCourse.COURSE_TITLE, courseTitle);
            classJson.put(AJEntityCourse.COURSE_VERSION, courseVersion);
            classDetails.add(classJson);
        });
        return classDetails;
    }

    private static void populateCourseTitleVersionForIds(List<String> courseIdList, Map<String, String> courseTitleMap,
        Map<String, String> courseVersionMap) {
        LazyList<AJEntityCourse> courses = AJEntityCourse.findBySQL(AJEntityCourse.SELECT_COURSE_TITLE_VERSION,
            Utils.convertListToPostgresArrayStringRepresentation(courseIdList));

        courses.forEach(course -> {
            courseTitleMap.put(course.getString(AJEntityCourse.ID), course.getString(AJEntityCourse.TITLE));
            courseVersionMap.put(course.getString(AJEntityCourse.ID), course.getString(AJEntityCourse.VERSION));
        });
    }

    private static List<String> fetchCourseIdsForClasses(LazyList<AJEntityClass> classes) {
        List<String> courseIdList = new ArrayList<>(classes.size());
        classes.stream().filter(classEntry -> classEntry.getString(AJEntityClass.COURSE_ID) != null)
            .forEach(classEntry -> {
                courseIdList.add(classEntry.getString(AJEntityClass.COURSE_ID));
            });
        return courseIdList;
    }

    private ExecutionResult<MessageResponse> populateTeacherDetails(JsonObject result) {
        try {
            LazyList<AJEntityUser> demographics =
                AJEntityUser.findBySQL(AJEntityUser.FETCH_TEACHER_DETAILS_QUERY,
                    Utils.convertListToPostgresArrayStringRepresentation(memberClassIdArray.getList()));
            // update that in the response
            JsonArray teacherDetails = new JsonArray(JsonFormatterBuilder
                .buildSimpleJsonFormatter(false, AJEntityUser.GET_SUMMARY_QUERY_FIELD_LIST).toJson(demographics));
            result.put(RESPONSE_BUCKET_TEACHER_DETAILS, teacherDetails);
            return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(result),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);

        } catch (DBException dbe) {
            LOGGER.warn("Unable to fetch teacher details for classes for user '{}'", context.userId(), dbe);
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
    }
}
