package org.mockito.internal.verification;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.internal.progress.VerificationModeImpl.*;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.RequiresValidState;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.HasStackTrace;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.progress.VerificationModeBuilder;

public class StrictlyMissingInvocationVerifierTest extends RequiresValidState {

    private StrictlyMissingInvocationVerifier verifier;
    private ReporterStub reporterStub;
    private InvocationMatcher wanted;
    private LinkedList<Invocation> invocations;
    private InvocationsFinderStub finderStub;
    
    @Before
    public void setup() {
        reporterStub = new ReporterStub();
        finderStub = new InvocationsFinderStub();
        verifier = new StrictlyMissingInvocationVerifier(finderStub, reporterStub);
        
        wanted = new InvocationBuilder().toInvocationMatcher();
        invocations = new LinkedList<Invocation>(asList(new InvocationBuilder().toInvocation()));
    }                                                                    

    @Test
    public void shouldNeverVerifyIfModeIsNotMissingModeStrictly() throws Exception {
        verifier.verify(null, null, atLeastOnce());
    }
    
    @Test
    public void shouldReportWantedButNotInvoked() throws Exception {
        assertTrue(finderStub.firstUnverifiedChunkToReturn.isEmpty());
        verifier.verify(invocations, wanted, new VerificationModeBuilder().strict());
        
        assertEquals(wanted.toString(), reporterStub.wanted);
    }
    
    @Test
    public void shouldReportWantedDiffersFromActual() throws Exception {
        Invocation different = new InvocationBuilder().differentMethod().toInvocation();
        finderStub.firstUnverifiedChunkToReturn.add(different);
        verifier.verify(invocations, wanted, new VerificationModeBuilder().strict());
        
        assertEquals(wanted.toString(), reporterStub.wanted);
        assertEquals(different.toString(), reporterStub.actual);
        assertSame(different.getStackTrace(), reporterStub.actualInvocationStackTrace);
    }
    
    class ReporterStub extends Reporter {
        private String wanted;
        private String actual;
        private HasStackTrace actualInvocationStackTrace;

        @Override public void strictlyWantedButNotInvoked(String wanted) {
            this.wanted = wanted;
        }
        
        @Override public void strictlyWantedDiffersFromActual(String wanted, String actual, HasStackTrace actualInvocationStackTrace) {
            this.wanted = wanted;
            this.actual = actual;
            this.actualInvocationStackTrace = actualInvocationStackTrace;
        }
    }
}