package vn.tnteco.repository.config.generator;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.tnteco.repository.data.constant.FieldConstant.*;

public class AuditGeneratorStrategy extends DefaultGeneratorStrategy {


    @Override
    public List<String> getJavaClassImplements(Definition definition, Mode mode) {
        System.out.println("=======start getJavaClassImplements=======");
        List<String> interfaces = super.getJavaClassImplements(definition, mode);
        if (mode == Mode.POJO && definition instanceof TableDefinition table) {
            Set<String> auditableColumnNames = Set.of(CREATED_AT, UPDATED_AT, CREATED_BY, UPDATED_BY);
            boolean hasOrgField = false;
            boolean hasAuditFields = table.getColumns()
                    .stream().map(Definition::getName)
                    .collect(Collectors.toSet())
                    .containsAll(auditableColumnNames);
            boolean hasSoftDeletableField = false;

            for (ColumnDefinition column : table.getColumns()) {
                if (ORG_ID.equalsIgnoreCase(column.getName())) {
                    hasOrgField = true;
                }
                if (DELETED_AT.equalsIgnoreCase(column.getName())) {
                    hasSoftDeletableField = true;
                }
            }

            System.out.println("hasOrgIdField: " + hasOrgField);
            System.out.println("hasAuditFields: " + hasAuditFields);
            System.out.println("hasSoftDeletableField: " + hasSoftDeletableField);
            if (hasOrgField) {
                interfaces.add("vn.tnteco.repository.data.audit.HasOrganization");
            }
            if (hasAuditFields) {
                interfaces.add("vn.tnteco.repository.data.audit.Auditable");
            }
            if (hasSoftDeletableField) {
                interfaces.add("vn.tnteco.repository.data.audit.SoftDeletable"); // Nếu có deleted_at
            }
        }
        return interfaces;
    }
}
