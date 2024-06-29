package org.sola.cs.services.ejb.system.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.sola.services.common.repository.DefaultSorter;
import org.sola.services.common.repository.Localized;
import org.sola.services.common.repository.entities.AbstractEntity;

@Table(schema = "system", name = "report")
@DefaultSorter(sortString="group_code, display_name")
public class ReportDescription extends AbstractEntity {
    @Id
    @Column
    private String id;
    @Localized
    @Column(name = "display_name")
    private String displayName;
    @Column
    private String description;
    @Column(name = "group_code")
    private String groupCode;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "display_in_menu")
    private Boolean displayInMenu;

    public ReportDescription(){
        id = UUID.randomUUID().toString();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getDisplayInMenu() {
        return displayInMenu;
    }

    public void setDisplayInMenu(Boolean displayInMenu) {
        this.displayInMenu = displayInMenu;
    }
}
