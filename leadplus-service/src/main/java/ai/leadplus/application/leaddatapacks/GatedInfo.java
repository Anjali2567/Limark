package ai.leadplus.application.leaddatapacks;

import lombok.Value;

import java.util.List;

@Value
public class GatedInfo {
    List<String> namedGatedSegments;
    boolean nullGated;
}
