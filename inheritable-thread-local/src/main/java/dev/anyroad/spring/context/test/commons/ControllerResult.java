package dev.anyroad.spring.context.test.commons;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ControllerResult {
    private int parentThreadValue;
    private int childThreadValue;
    private boolean childThreadHasSameRequestAttributes;
    private boolean success;
    private String execptionMessage;
}
