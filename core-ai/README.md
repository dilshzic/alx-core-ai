# Core AI Module

A robust, unified AI abstraction layer for Android applications, designed to provide a seamless interface for interacting with multiple Large Language Model (LLM) providers like **Google Gemini** and **Groq (OpenAI Compatible)**.

## 🚀 Features

- **Unified Interface**: Interact with different AI engines through a single `AiBrain` interface.
- **Multi-Provider Support**:
  - **Google Gemini**: Full support for Pro/Flash models, Search Grounding, Code Execution, and Safety Thresholds.
  - **Groq**: High-speed inference for Llama, Mixtral, and DeepSeek models with Reasoning Effort control.
- **Advanced Configuration**: Fine-tune parameters like Temperature, Top P, Max Tokens, and Penalties.
- **Reasoning Controls**: Manage "thinking" depth for models like DeepSeek-R1 and Gemini Thinking models.
- **Persistence Layer**: Automatic state management and configuration storage using Jetpack DataStore.
- **Usage Analytics**: Built-in tracking for token consumption and request history.
- **Model Registry**: Dynamic model discovery via a central JSON/CSV catalog.

## 📁 Architecture

The module is organized into logical layers:

- **`AiBrainManager`**: The primary entry point for the application. Handles model building and usage logging.
- **`BrainRegistry`**: Manages the catalog of available models and providers.
- **`StoredBrainConfig`**: Data class representing the full state of the AI engine.
- **`implementations/`**: Concrete SDK integrations (e.g., `GeminiBrainImpl`).
- **`models/`**: Domain models, enums (Safety, Reasoning, Provider), and capabilities.

## 🛠️ Getting Started

### 1. Configure API Keys
Add your API keys to the root `local.properties` file:
```properties
zynera-gemini-key=YOUR_GEMINI_KEY
zynera-groq-key=YOUR_GROQ_KEY
```

### 2. Dependency Injection (Hilt)
Inject the `AiBrainManager` into your ViewModels:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val aiBrainManager: AiBrainManager
) : ViewModel() { ... }
```

### 3. Usage Examples

#### Simple Text Generation
```kotlin
val result = aiBrainManager.askBrain("Hello, how are you?")
result.onSuccess { text -> 
    println(text)
}.onFailure { error -> 
    println("Error: ${error.message}")
}
```

#### JSON Mode (Structured Output)
```kotlin
val jsonResult = aiBrainManager.askBrain("List 3 fruits in JSON format", isJsonMode = true)
```

#### Real-time Streaming
```kotlin
aiBrainManager.streamFromBrain("Write a story...")
    .collect { chunk ->
        // Update UI in real-time
    }
```

## ⚙️ Advanced Configuration

The module supports deep tuning through the `BrainDataStoreManager`:
- **Identity**: System instructions and personas.
- **Tools**: Toggle Google Search and Python code execution.
- **Safety**: 4-level granular filtering for harassment, hate speech, etc.
- **Reasoning**: Control CoT (Chain of Thought) depth for R1/Thinking models.

## 📊 Analytics & Usage

Track your AI usage via the `TotalUsageSummary` and `ModelUsageSummary` models, which aggregate token counts and request frequencies across all providers.

---
*Maintained by the AlgorithmX Core AI Team.*
