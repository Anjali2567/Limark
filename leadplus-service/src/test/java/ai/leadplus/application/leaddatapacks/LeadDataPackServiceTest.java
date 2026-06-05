package ai.leadplus.application.leaddatapacks;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.industries.IndustryService;
import ai.leadplus.domain.leaddatapacks.LeadDataPack;
import ai.leadplus.domain.leaddatapacks.LeadDataPackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadDataPackServiceTest {

    @Mock
    private LeadDataPackRepository repository;

    @Mock
    private IndustryService industryService;

    @InjectMocks
    private LeadDataPackService service;

    private LeadDataPack entity;
    private LeadDataPackDto dto;

    @BeforeEach
    void setup() {
        entity = LeadDataPack.builder()
                .id(1L)
                .name("Pack 1")
                .industryIds(List.of(1L, 2L))
                .active(true)
                .build();

        dto = LeadDataPackDto.builder()
                .id(1L)
                .name("Pack 1")
                .industryIds(List.of(1L, 2L))
                .active(true)
                .build();
    }

    @Test
    void createLeadDataPack_shouldSaveAndReturnDto() {
        when(industryService.areIndustriesActive(anyList())).thenReturn(true);
        when(repository.save(any())).thenReturn(entity);

        LeadDataPackDto result = service.createLeadDataPack(dto);

        assertThat(result.getName()).isEqualTo("Pack 1");
        verify(repository).save(any(LeadDataPack.class));
    }


    @Test
    void getById_shouldReturnDto_whenExists() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(entity));

        LeadDataPackDto result = service.getById("1");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Pack 1");
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LeadDataPack not found");
    }

    @Test
    void getAllLeadDataPacks_shouldReturnActiveList() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<LeadDataPack> page = new PageImpl<>(List.of(entity));

        when(repository.findAllByActiveTrue(pageable)).thenReturn(page);

        Page<LeadDataPackDto> result = service.getAllLeadDataPacks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
    }


    @Test
    void updateLeadDataPack_shouldUpdateAndReturnDto() {

        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(LeadDataPack.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(industryService.areIndustriesActive(anyList())).thenReturn(true);

        LeadDataPackDto updateDto = LeadDataPackDto.builder()
                .name("Updated Pack")
                .industryIds(List.of(3L))
                .active(true)
                .build();

        ArgumentCaptor<LeadDataPack> captor = ArgumentCaptor.forClass(LeadDataPack.class);

        LeadDataPackDto result = service.updateLeadDataPack("1", updateDto);

        verify(repository).save(captor.capture());

        LeadDataPack savedEntity = captor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("Updated Pack");
        assertThat(savedEntity.getIndustryIds()).containsExactly(3L);
        assertThat(savedEntity.isActive()).isTrue();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Pack");
        assertThat(result.getIndustryIds()).containsExactly(3L);
    }


    @Test
    void updateLeadDataPack_shouldThrow_whenNotFound() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        when(industryService.areIndustriesActive(anyList())).thenReturn(true);

        assertThatThrownBy(() ->
                service.updateLeadDataPack("1", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    void deleteLeadDataPackById_shouldSoftDelete() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(entity));

        service.deleteLeadDataPackById("1");

        assertThat(entity.isActive()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void deleteLeadDataPackById_shouldThrow_whenNotFound() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.deleteLeadDataPackById("1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
