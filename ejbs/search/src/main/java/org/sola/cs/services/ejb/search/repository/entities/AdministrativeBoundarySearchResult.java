package org.sola.cs.services.ejb.search.repository.entities;

import javax.persistence.Column;
import org.sola.services.common.repository.CommonSqlProvider;
import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;

public class AdministrativeBoundarySearchResult extends AbstractReadOnlyEntity {

    @Column
    private String id;
    @Column
    private String name;
    @Column(name = "type_code")
    private String typeCode;
    @Column(name = "type_name")
    private String typeName;
    @Column(name = "authority_name")
    private String authorityName;
    @Column(name = "parent_id")
    private String parentId;
    @Column(name = "status_code")
    private String statusCode;
    @Column(name = "status_name")
    private String statusName;
    @Column
    private int level;
    @Column
    private String path;

    public static final String PARAM_PARENT_ID = "parentId";
    public static final String PARAM_ID = "id";

    private static final String SELECT_PART = ""
            + "WITH RECURSIVE all_administrative_boundaries AS (\n "
            + "  SELECT id, name, type_code, authority_name, parent_id, status_code, 1 as level, array[ROW_NUMBER() OVER (ORDER BY name)] AS path\n "
            + "  FROM opentenure.administrative_boundary \n "
            + "  WHERE parent_id is null\n "
            + " UNION \n "
            + "	 SELECT b.id, b.name, b.type_code, b.authority_name, b.parent_id, b.status_code, ab.level + 1 as level, ab.path || (ROW_NUMBER() OVER (ORDER BY b.name)) AS path\n "
            + "  FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id\n "
            + ")\n "
            + "SELECT b.id, b.name, b.type_code, get_translation(bt.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as type_name, b.authority_name, b.parent_id, b.status_code, get_translation(bs.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as status_name, b.level, b.path::varchar \n"
            + "FROM (all_administrative_boundaries b INNER JOIN opentenure.administrative_boundary_type bt ON b.type_code = bt.code) INNER JOIN opentenure.administrative_boundary_status bs ON b.status_code = bs.code ";

    public static final String QUERY_GET_ALL = SELECT_PART + "ORDER BY b.path, b.level";
    
    public static final String QUERY_GET_APPROVED = ""
            + "WITH RECURSIVE all_administrative_boundaries AS (\n "
            + "  SELECT id, name, type_code, authority_name, parent_id, status_code, 1 as level, array[ROW_NUMBER() OVER (ORDER BY name)] AS path\n "
            + "  FROM opentenure.administrative_boundary \n "
            + "  WHERE parent_id is null and status_code = 'approved'\n "
            + " UNION \n "
            + "	 SELECT b.id, b.name, b.type_code, b.authority_name, b.parent_id, b.status_code, ab.level + 1 as level, ab.path || (ROW_NUMBER() OVER (ORDER BY b.name)) AS path\n "
            + "  FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id\n "
            + ")\n "
            + "SELECT b.id, b.name, b.type_code, get_translation(bt.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as type_name, b.authority_name, b.parent_id, b.status_code, get_translation(bs.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as status_name, b.level, b.path::varchar \n"
            + "FROM (all_administrative_boundaries b INNER JOIN opentenure.administrative_boundary_type bt ON b.type_code = bt.code) INNER JOIN opentenure.administrative_boundary_status bs ON b.status_code = bs.code "
            + "WHERE b.status_code = 'approved' ORDER BY b.path, b.level";

    public static final String QUERY_GET_ALL_PARENTS = SELECT_PART
            + "WHERE type_code not in (select code from opentenure.administrative_boundary_type order by level desc limit 1)\n "
            + "ORDER BY b.path, b.level";

    public static final String QUERY_GET_CHILDREN = ""
            + "WITH RECURSIVE all_administrative_boundaries AS (\n "
            + "  SELECT id, name, type_code, authority_name, parent_id, status_code, 1 as level, array[ROW_NUMBER() OVER (ORDER BY name)] AS path\n "
            + "  FROM opentenure.administrative_boundary \n "
            + "  WHERE parent_id = #{" + PARAM_PARENT_ID + "}\n "
            + " UNION \n "
            + "	 SELECT b.id, b.name, b.type_code, b.authority_name, b.parent_id, b.status_code, ab.level + 1 as level, ab.path || (ROW_NUMBER() OVER (ORDER BY b.name)) AS path\n "
            + "  FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.parent_id = ab.id\n "
            + ")\n "
            + "SELECT b.id, b.name, b.type_code, get_translation(bt.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as type_name, b.authority_name, b.parent_id, b.status_code, get_translation(bs.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as status_name, b.level, b.path::varchar \n"
            + "FROM (all_administrative_boundaries b INNER JOIN opentenure.administrative_boundary_type bt ON b.type_code = bt.code) INNER JOIN opentenure.administrative_boundary_status bs ON b.status_code = bs.code "
            + "ORDER BY b.path, b.level";

    public static final String QUERY_GET_PARENTS = ""
            + "WITH RECURSIVE all_administrative_boundaries AS (\n"
            + "        SELECT id, name, type_code, authority_name, parent_id, status_code, 1 as level, array[ROW_NUMBER() OVER (ORDER BY name)] AS path \n"
            + "        FROM opentenure.administrative_boundary \n"
            + "        WHERE id = #{" + PARAM_ID + "} \n"
            + "      UNION \n"
            + "	SELECT b.id, b.name, b.type_code, b.authority_name, b.parent_id, b.status_code, ab.level + 1 as level, ab.path || (ROW_NUMBER() OVER (ORDER BY b.name)) AS path \n"
            + "        FROM opentenure.administrative_boundary b inner join all_administrative_boundaries ab on b.id = ab.parent_id \n"
            + ") \n"
            + "SELECT b.path::varchar, b.id, b.name, b.type_code, get_translation(bt.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as type_name, b.authority_name, b.parent_id, b.status_code, get_translation(bs.display_value, #{" + CommonSqlProvider.PARAM_LANGUAGE_CODE + "}) as status_name, b.level \n"
            + "FROM (all_administrative_boundaries b INNER JOIN opentenure.administrative_boundary_type bt ON b.type_code = bt.code) INNER JOIN opentenure.administrative_boundary_status bs ON b.status_code = bs.code \n"
            + "ORDER BY b.level DESC;";

    public AdministrativeBoundarySearchResult() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
