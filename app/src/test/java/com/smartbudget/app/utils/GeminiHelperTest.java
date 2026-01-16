package com.smartbudget.app.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for GeminiHelper class.
 * Tests model management, provider switching, and input validation.
 * 
 * Note: API-dependent tests are marked with @Ignore since they require network.
 * Use these tests for local development validation.
 */
@RunWith(MockitoJUnitRunner.class)
public class GeminiHelperTest {

    private GeminiHelper geminiHelper;

    @Before
    public void setUp() {
        // Reset singleton before each test
        GeminiHelper.resetInstance();
        // Get instance without context (for testing)
        geminiHelper = GeminiHelper.getInstance(null);
    }

    @After
    public void tearDown() {
        GeminiHelper.resetInstance();
    }

    // ==================== Provider Tests ====================

    @Test
    public void testDefaultProviderIsGroq() {
        assertEquals(GeminiHelper.AIProvider.GROQ, geminiHelper.getCurrentProvider());
    }

    @Test
    public void testSetProviderToGemini() {
        geminiHelper.setProvider(GeminiHelper.AIProvider.GEMINI);
        assertEquals(GeminiHelper.AIProvider.GEMINI, geminiHelper.getCurrentProvider());
    }

    @Test
    public void testSetProviderToGroq() {
        geminiHelper.setProvider(GeminiHelper.AIProvider.GROQ);
        assertEquals(GeminiHelper.AIProvider.GROQ, geminiHelper.getCurrentProvider());
    }

    @Test
    public void testProviderDisplayGroq() {
        geminiHelper.setProvider(GeminiHelper.AIProvider.GROQ);
        assertEquals("Groq", geminiHelper.getProviderDisplay());
    }

    @Test
    public void testProviderDisplayGemini() {
        geminiHelper.setProvider(GeminiHelper.AIProvider.GEMINI);
        assertEquals("Gemini", geminiHelper.getProviderDisplay());
    }

    // ==================== Model Management Tests ====================

    @Test
    public void testGetAllModelsNotEmpty() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        assertNotNull(models);
        assertFalse(models.isEmpty());
    }

    @Test
    public void testGetAllModelsContainsGroq() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        boolean hasGroq = false;
        for (GeminiHelper.ModelInfo model : models) {
            if (model.provider == GeminiHelper.AIProvider.GROQ) {
                hasGroq = true;
                break;
            }
        }
        assertTrue("Should contain Groq models", hasGroq);
    }

    @Test
    public void testGetAllModelsContainsGemini() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        boolean hasGemini = false;
        for (GeminiHelper.ModelInfo model : models) {
            if (model.provider == GeminiHelper.AIProvider.GEMINI) {
                hasGemini = true;
                break;
            }
        }
        assertTrue("Should contain Gemini models", hasGemini);
    }

    @Test
    public void testDefaultSelectedModelIsNull() {
        assertNull(geminiHelper.getSelectedModel());
    }

    @Test
    public void testDefaultSelectedModelNameIsAuto() {
        assertEquals("Tự Động", geminiHelper.getSelectedModelName());
    }

    @Test
    public void testSetSelectedModelChangesProvider() {
        // Set a Gemini model
        geminiHelper.setSelectedModel("gemini-2.0-flash");
        assertEquals(GeminiHelper.AIProvider.GEMINI, geminiHelper.getCurrentProvider());
        
        // Set a Groq model
        geminiHelper.setSelectedModel("llama-3.3-70b-versatile");
        assertEquals(GeminiHelper.AIProvider.GROQ, geminiHelper.getCurrentProvider());
    }

    @Test
    public void testGetSelectedModelReturnsSetValue() {
        String modelId = "gemini-1.5-pro";
        geminiHelper.setSelectedModel(modelId);
        assertEquals(modelId, geminiHelper.getSelectedModel());
    }

    // ==================== Model Info Tests ====================

    @Test
    public void testModelInfoHasValidFields() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        for (GeminiHelper.ModelInfo model : models) {
            assertNotNull("Model ID should not be null", model.id);
            assertNotNull("Model name should not be null", model.name);
            assertNotNull("Model provider should not be null", model.provider);
            assertFalse("Model ID should not be empty", model.id.isEmpty());
            assertFalse("Model name should not be empty", model.name.isEmpty());
        }
    }

    @Test
    public void testGroqModelsHaveCorrectProvider() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        for (GeminiHelper.ModelInfo model : models) {
            if (model.id.contains("llama") || model.id.contains("mixtral") || model.id.contains("gemma")) {
                assertEquals("Llama/Mixtral/Gemma should be Groq", 
                    GeminiHelper.AIProvider.GROQ, model.provider);
            }
        }
    }

    @Test
    public void testGeminiModelsHaveCorrectProvider() {
        List<GeminiHelper.ModelInfo> models = geminiHelper.getAllModels();
        for (GeminiHelper.ModelInfo model : models) {
            if (model.id.startsWith("gemini-")) {
                assertEquals("Gemini models should have GEMINI provider", 
                    GeminiHelper.AIProvider.GEMINI, model.provider);
            }
        }
    }

    // ==================== Input Validation Tests ====================

    @Test
    public void testGetFinancialAdviceWithEmptyMessage() {
        final boolean[] errorCalled = {false};
        
        geminiHelper.getFinancialAdvice("", new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                fail("Should not succeed with empty message");
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                assertTrue(error.contains("không được để trống"));
            }
        });
        
        assertTrue("Error callback should be called", errorCalled[0]);
    }

    @Test
    public void testGetFinancialAdviceWithWhitespaceMessage() {
        final boolean[] errorCalled = {false};
        
        geminiHelper.getFinancialAdvice("   ", new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                fail("Should not succeed with whitespace message");
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
            }
        });
        
        assertTrue("Error callback should be called", errorCalled[0]);
    }

    @Test
    public void testAnalyzeSpendingWithEmptyData() {
        final boolean[] errorCalled = {false};
        
        geminiHelper.analyzeSpending("", new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                fail("Should not succeed with empty data");
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                assertTrue(error.contains("không hợp lệ"));
            }
        });
        
        assertTrue("Error callback should be called", errorCalled[0]);
    }

    @Test
    public void testSuggestBudgetWithZeroIncome() {
        final boolean[] errorCalled = {false};
        
        geminiHelper.suggestBudget(0, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                fail("Should not succeed with zero income");
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
                assertTrue(error.contains("lớn hơn 0"));
            }
        });
        
        assertTrue("Error callback should be called", errorCalled[0]);
    }

    @Test
    public void testSuggestBudgetWithNegativeIncome() {
        final boolean[] errorCalled = {false};
        
        geminiHelper.suggestBudget(-1000, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                fail("Should not succeed with negative income");
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
            }
        });
        
        assertTrue("Error callback should be called", errorCalled[0]);
    }

    // ==================== History Management Tests ====================

    @Test
    public void testClearHistoryDoesNotThrow() {
        // Should not throw any exceptions
        geminiHelper.clearHistory();
        // Call again to test with already null history
        geminiHelper.clearHistory();
    }

    // ==================== Singleton Tests ====================

    @Test
    public void testSingletonReturnsSameInstance() {
        GeminiHelper instance1 = GeminiHelper.getInstance(null);
        GeminiHelper instance2 = GeminiHelper.getInstance(null);
        assertSame(instance1, instance2);
    }

    @Test
    public void testResetInstanceCreatesNewInstance() {
        GeminiHelper instance1 = GeminiHelper.getInstance(null);
        GeminiHelper.resetInstance();
        GeminiHelper instance2 = GeminiHelper.getInstance(null);
        assertNotSame(instance1, instance2);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testSetNullSelectedModel() {
        geminiHelper.setSelectedModel("gemini-2.0-flash");
        geminiHelper.setSelectedModel(null);
        assertNull(geminiHelper.getSelectedModel());
        assertEquals("Tự Động", geminiHelper.getSelectedModelName());
    }

    @Test
    public void testSetUnknownModelId() {
        String unknownModel = "unknown-model-xyz";
        geminiHelper.setSelectedModel(unknownModel);
        assertEquals(unknownModel, geminiHelper.getSelectedModel());
        assertEquals(unknownModel, geminiHelper.getSelectedModelName());
    }

    // ==================== Callback Interface Tests ====================

    @Test
    public void testCallbackInterfaceMethods() {
        // Just verify the interface compiles and can be implemented
        GeminiHelper.GeminiCallback callback = new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                assertNotNull(response);
            }

            @Override
            public void onError(String error) {
                assertNotNull(error);
            }
        };
        
        callback.onSuccess("test response");
        callback.onError("test error");
    }
}
