package com.wind.demo;


import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import leakcanary.EventListener;
import shark.HeapAnalysisSuccess;
import shark.Leak;
import shark.LeakTrace;
import shark.LibraryLeak;

class SentryLeakUploader implements EventListener {

    public void onEvent(Event event) {
        if (event instanceof Event.HeapAnalysisDone<?>) {
            Event.HeapAnalysisDone<?> heapAnalysisDone = (Event.HeapAnalysisDone<?>) event;
            if (!(heapAnalysisDone.getHeapAnalysis() instanceof HeapAnalysisSuccess)) return;
            HeapAnalysisSuccess heapAnalysis = (HeapAnalysisSuccess) heapAnalysisDone.getHeapAnalysis();
            List<Pair<Leak, LeakTrace>> allLeakTraces = new ArrayList<>();
            Iterator<Leak> iterator = heapAnalysis.getAllLeaks().iterator();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                iterator.forEachRemaining(new Consumer<Leak>() {
                    @Override
                    public void accept(Leak leak) {
                        List<LeakTrace> leakTraces = leak.getLeakTraces();
                        for (LeakTrace leakTrace : leak.getLeakTraces()) {
                            allLeakTraces.add(new Pair<Leak, LeakTrace>(leak, leakTrace));
                        }
                    }
                });
            }

            for (Pair<Leak, LeakTrace> pair : allLeakTraces) {
                Leak leak = pair.first;
                LeakTrace leakTrace = pair.second;
                RuntimeException exception = new FakeReportingException(leak.getShortDescription());
                SentryEvent sentryEvent = new SentryEvent(exception);
                HashMap<String, Object> leakContexts = new HashMap<String, Object>();
                addHeapAnalysis(heapAnalysis, leakContexts);
                addLeak(leak, leakContexts);
                addLeakTrace(leakTrace, leakContexts);
                sentryEvent.getContexts().put("Leak", leakContexts);
                // grouping leaks
                ArrayList<String> objects = new ArrayList<>();
                String signature = leak.getSignature();
                objects.add(signature);
                sentryEvent.setFingerprints(objects);
                Sentry.captureEvent(sentryEvent);
            }
        }
    }

    private static void addHeapAnalysis(
            HeapAnalysisSuccess heapAnalysis,
            HashMap<String, Object> leakContexts
    ) {
        leakContexts.put("heapDumpPath", heapAnalysis.getHeapDumpFile().getAbsolutePath());
        leakContexts.putAll(heapAnalysis.getMetadata());
        leakContexts.put("analysisDurationMs", heapAnalysis.getAnalysisDurationMillis());
    }

    private static void addLeak(Leak leak, HashMap<String, Object> leakContexts) {
        leakContexts.put("libraryLeak", leak instanceof LibraryLeak);
        if (leak instanceof LibraryLeak) {
            LibraryLeak libraryLeak = (LibraryLeak) leak;
            leakContexts.put("libraryLeakPattern", libraryLeak.getPattern().toString());
            leakContexts.put("libraryLeakDescription", libraryLeak.getDescription());
        }
    }

    private static void addLeakTrace(LeakTrace leakTrace, HashMap<String, Object> leakContexts) {
        if (leakTrace.getRetainedHeapByteSize() != null) {
            leakContexts.put("retainedHeapByteSize", leakTrace.getRetainedHeapByteSize());
        }
        leakContexts.put("signature", leakTrace.getSignature());
        leakContexts.put("leakTrace", leakTrace.toString());
    }

    public static class FakeReportingException extends RuntimeException {
        public FakeReportingException(String message) {
            super(message);
        }
    }
}
