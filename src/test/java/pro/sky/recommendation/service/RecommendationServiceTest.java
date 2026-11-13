package pro.sky.recommendation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.recommendation.dto.BankUser;
import pro.sky.recommendation.dto.RecommendationDto;
import pro.sky.recommendation.dto.RecommendationsResponse;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.model.RuleQuery;
import pro.sky.recommendation.repository.DynamicRuleRepository;
import pro.sky.recommendation.repository.RecommendationsRepository;
import pro.sky.recommendation.rules.RecommendationRuleSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID PRODUCT = UUID.randomUUID();

    @Mock
    private DynamicRuleRepository ruleRepository;
    @Mock
    private RuleQueryEvaluator evaluator;
    @Mock
    private RuleStatService statService;
    @Mock
    private RecommendationsRepository recommendationsRepository;

    private static DynamicRule dynamicRule(UUID productId) {
        DynamicRule rule = new DynamicRule();
        rule.setId(UUID.randomUUID());
        rule.setProductId(productId);
        rule.setProductName("Dynamic");
        rule.setProductText("text");
        rule.setRule(List.of(new RuleQuery()));
        return rule;
    }

    @Test
    void combinesStaticAndDynamicRulesWithoutDuplicates() {
        RecommendationDto staticDto = new RecommendationDto("Static", PRODUCT, "text");
        RecommendationRuleSet staticRule = userId -> Optional.of(staticDto);
        RecommendationRuleSet emptyRule = userId -> Optional.empty();

        UUID otherProduct = UUID.randomUUID();
        when(ruleRepository.findAll()).thenReturn(List.of(dynamicRule(PRODUCT), dynamicRule(otherProduct)));
        when(evaluator.evaluate(any(), any())).thenReturn(true);

        DynamicRecommendationService dynamicService =
                new DynamicRecommendationService(ruleRepository, evaluator, statService);
        RecommendationService service =
                new RecommendationService(List.of(staticRule, emptyRule), dynamicService);

        RecommendationsResponse response = service.getRecommendations(USER);

        assertThat(response.getUserId()).isEqualTo(USER);
        assertThat(response.getRecommendations())
                .extracting(RecommendationDto::getId)
                .containsExactly(PRODUCT, otherProduct);
        assertThat(response.getRecommendations().get(0).getName()).isEqualTo("Static");
    }

    @Test
    void dynamicRuleIncrementsStatisticsOnlyWhenSatisfied() {
        DynamicRule rule = dynamicRule(PRODUCT);
        when(ruleRepository.findAll()).thenReturn(List.of(rule));
        when(evaluator.evaluate(any(), any())).thenReturn(false);

        DynamicRecommendationService dynamicService =
                new DynamicRecommendationService(ruleRepository, evaluator, statService);

        assertThat(dynamicService.check(USER)).isEmpty();
        verify(statService, never()).increment(any());

        when(evaluator.evaluate(any(), any())).thenReturn(true);

        assertThat(dynamicService.check(USER)).hasSize(1);
        verify(statService).increment(rule.getId());
    }

    @Test
    void userServiceReturnsOnlyUniqueUser() {
        BankUser user = new BankUser(USER, "Иван", "Иванов");
        when(recommendationsRepository.findUsersByUsername("ivan")).thenReturn(List.of(user));
        when(recommendationsRepository.findUsersByUsername("twins")).thenReturn(List.of(user, user));
        when(recommendationsRepository.findUsersByUsername("nobody")).thenReturn(List.of());

        UserService userService = new UserService(recommendationsRepository);

        assertThat(userService.findUniqueByUsername("ivan")).contains(user);
        assertThat(userService.findUniqueByUsername("twins")).isEmpty();
        assertThat(userService.findUniqueByUsername("nobody")).isEmpty();
    }
}
