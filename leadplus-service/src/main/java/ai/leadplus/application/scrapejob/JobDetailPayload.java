package ai.leadplus.application.scrapejob;

import lombok.Data;

import java.util.List;

@Data
public class JobDetailPayload {
    private List<String> requirements;
    private List<String> technologies;
    private List<String> tools;
    private List<String> services;
}
