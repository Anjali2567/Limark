package ai.leadplus.domain.tenantcompanies;

import ai.leadplus.domain.common.CRM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantCompanyRepository extends JpaRepository<TenantCompany, Long> {

    List<TenantCompany> findAllByTenantIdAndSourceTypeAndSourceIdIn(Long tenantId, CRM sourceType, List<String> sourceIds);
}
