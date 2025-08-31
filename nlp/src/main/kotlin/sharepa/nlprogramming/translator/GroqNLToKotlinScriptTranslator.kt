package sharepa.nlprogramming.translator

import sharepa.nlprogramming.llm.GroqLLMClient

internal class GroqNLToKotlinScriptTranslator : AbstractLlmNLToKotlinScriptTranslator(GroqLLMClient())