package ai.leadplus.application.useractivitylogs;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.users.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActivityAspect {

    private final UserActivityLogService userActivityLogService;
    private final UserValidator userValidator;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer =
            new DefaultParameterNameDiscoverer();

    @AfterReturning(
            value = "@annotation(trackUserActivity)",
            returning = "result"
    )
    public void logActivity(JoinPoint joinPoint,
                            TrackUserActivity trackUserActivity,
                            Object result) {

        try {
            UserDto userDto = userValidator.getAuthenticatedUser();

            String evaluatedMessage = evaluateMessage(
                    trackUserActivity.message(),
                    joinPoint,
                    result
            );

            userActivityLogService.log(
                    userDto.getTenantId(),
                    userDto.getWorkspaceId(),
                    userDto.getId(),
                    userDto.getName(),
                    evaluatedMessage,
                    trackUserActivity.level(),
                    trackUserActivity.type()
            );

        } catch (Exception e) {
            log.error("Failed to log user activity", e);
        }
    }

    private String evaluateMessage(String message,
                                   JoinPoint joinPoint,
                                   Object result) {

        if (!message.contains("#{")) {
            return message;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(
                        joinPoint.getTarget(),
                        method,
                        joinPoint.getArgs(),
                        nameDiscoverer
                );

        context.setVariable("result", result);

        Expression expression = parser.parseExpression(
                 message,
                new TemplateParserContext()
        );

        return expression.getValue(context, String.class);
    }
}