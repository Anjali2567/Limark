package ai.leadplus.domain.tenantcontacts;

import ai.leadplus.domain.common.CRM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TenantContactRepository extends JpaRepository<TenantContact, Long>, JpaSpecificationExecutor<TenantContact> {

    List<TenantContact> findAllByTenantIdAndEmailIn(Long tenantId, List<String> emails);

    List<TenantContact> findAllByTenantIdAndSourceTypeAndEmailIn(Long tenantId, CRM sourceType, List<String> emails);

    List<TenantContact> findAllByTenantIdAndIdIn(Long tenantId, List<Long> ids);
}
