package com.resolveai.ai;

/** Abstraction over any AI provider. The rest of the application (ComplaintService)
 *  only ever talks to this interface, never to a specific provider, so:
 *    - the provider can be swapped (mock -> OpenAI -> anything else) via a config flag
 *    - AI failures/timeouts are always handled uniformly
 *    - the core complaint workflow never breaks if AI is unavailable
 */
public interface AIService {

    /** Analyze a complaint's text and suggest category/sub-category/priority/summary. */
    AIAnalysisResult analyzeComplaint(String title, String description);

    /** Generate a short resolution suggestion based on the complaint text and
     *  descriptions of similar previously-resolved complaints. Always a suggestion -
     *  never used to auto-resolve anything. */
    String suggestResolution(String complaintText, String similarResolvedText);
}
