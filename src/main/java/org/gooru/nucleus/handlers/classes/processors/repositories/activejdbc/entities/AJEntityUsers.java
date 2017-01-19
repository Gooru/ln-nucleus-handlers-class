package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ashish on 4/3/16.
 */
@Table("users")
public class AJEntityUsers extends Model {
    public static final String GET_SUMMARY_QUERY =
        "select id, first_name, last_name, thumbnail, roster_global_userid from users where id = ANY(?::uuid[])";
    public static final String FETCH_TEACHER_DETAILS_QUERY =
        "select id, first_name, last_name, thumbnail from users where id = ANY(select creator_id from "
            + "class where id = ANY(?::uuid[]))";

    public static final String ID = "id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String THUMBNAIL = "thumbnail";
    public static final String ROSTER_GLOBAL_USERID = "roster_global_userid";
    public static final List<String> GET_SUMMARY_QUERY_FIELD_LIST =
        Arrays.asList(ID, FIRST_NAME, LAST_NAME, THUMBNAIL, ROSTER_GLOBAL_USERID);

}
