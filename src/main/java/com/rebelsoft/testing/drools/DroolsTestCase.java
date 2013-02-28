package com.rebelsoft.testing.drools;

import junit.framework.TestCase;
import junit.framework.TestResult;
import org.drools.definition.KnowledgePackage;
import org.drools.event.rule.*;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import java.util.*;

public abstract class DroolsTestCase extends TestCase implements AgendaEventListener, WorkingMemoryEventListener {
// ------------------------------ FIELDS ------------------------------

    static int ruleCount = 0;
    static Set<String> ruleNames = null;

    protected List<Object> insertedList = new LinkedList<Object>();
    protected List<Object> updatedList = new LinkedList<Object>();
    protected List<Object> retractedList = new LinkedList<Object>();
    protected StatefulKnowledgeSession session;
    protected List<String> firedList = new ArrayList<String>();

// -------------------------- STATIC METHODS --------------------------

    private static void afterClass(DroolsTestCase droolsTestCase) {
        if (ruleNames == null) {
            return;
        }

        final int ranCount = ruleCount - ruleNames.size();
        final double coveragePercent = (double) ranCount / ruleCount * 100;
        System.out.println("Converage:" + ranCount + "/" + ruleCount + " (" + coveragePercent + "%)");
        if (ruleNames.size() > 0) {
            System.out.println("Rules Not Tested:" + ruleNames);
        }
    }

    private static void beforeClass(DroolsTestCase droolsTestCase) {
        if (ruleNames != null) {
            return;
        }
        ruleNames = new HashSet<String>();
        final StatefulKnowledgeSession session = droolsTestCase.setUpSession();
        if (session != null) {
            final Collection<KnowledgePackage> packages = session.getKnowledgeBase().getKnowledgePackages();
            for (KnowledgePackage aPackage : packages) {
                final Collection<org.drools.definition.rule.Rule> rules = aPackage.getRules();
                for (org.drools.definition.rule.Rule rule : rules) {
                    ruleNames.add(rule.getName());
                }
            }
            ruleCount = ruleNames.size();
            session.dispose();
        }
    }

    protected abstract StatefulKnowledgeSession setUpSession();

// --------------------------- CONSTRUCTORS ---------------------------

    public DroolsTestCase() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AgendaEventListener ---------------------


    @Override
    public void activationCreated(ActivationCreatedEvent activationCreatedEvent) {
    }

    @Override
    public void activationCancelled(ActivationCancelledEvent activationCancelledEvent) {
    }

    @Override
    public void beforeActivationFired(BeforeActivationFiredEvent event) {
        final String name = event.getActivation().getRule().getName();
        ruleNames.remove(name);
    }

    @Override
    public void afterActivationFired(AfterActivationFiredEvent event) {
        String name = event.getActivation().getRule().getName();
        System.out.println("fired  " + name);
        firedList.add(name);
        if (firedList.size() > 1000) {
            throw new IllegalStateException("Possibly infinite loop!");
        }
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent agendaGroupPoppedEvent) {
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent agendaGroupPushedEvent) {
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent ruleFlowGroupActivatedEvent) {
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent ruleFlowGroupActivatedEvent) {
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent ruleFlowGroupDeactivatedEvent) {
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent ruleFlowGroupDeactivatedEvent) {
    }

// --------------------- Interface Test ---------------------

    public void run(TestResult result) {
        beforeClass(this);

        super.run(result);

        afterClass(this);
    }

// --------------------- Interface WorkingMemoryEventListener ---------------------

    public void objectInserted(ObjectInsertedEvent event) {
        insertedList.add(event.getObject());
    }

    public void objectUpdated(ObjectUpdatedEvent event) {
        updatedList.add(event.getObject());
    }

    public void objectRetracted(ObjectRetractedEvent event) {
        retractedList.add(event.getOldObject());
    }

// -------------------------- OTHER METHODS --------------------------

    protected void assertFactCount(int count) {
        final Iterator iterator = session.getObjects().iterator();
        int factCount = 0;
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            factCount++;
        }
        assertEquals(count, factCount);
    }

    protected void assertFiredRule(final String ruleName) {
        assertTrue(firedList.contains(ruleName));
    }

    protected void assertFiredRulesInOrder(final String... ruleNames) {
        assertEquals(Arrays.asList(ruleNames), firedList);
    }

    protected void assertInsertedInOrder(final Object... facts) {
        assertEquals(Arrays.asList(facts), insertedList);
    }

    protected void assertRetractedInOrder(final Object... facts) {
        assertEquals(Arrays.asList(facts), retractedList);
    }

    protected void assertUpdatedInOrder(final Object... facts) {
        assertEquals(Arrays.asList(facts), updatedList);
    }

    public boolean didAnyRulesFired() {
        return firedList.size() > 0;
    }

    protected FactHandle insertAndFire(Object fact) {
        final FactHandle handle = session.insert(fact);
        session.fireAllRules();
        return handle;
    }

    protected void setUp() throws Exception {
        super.setUp();
        session = setUpSession();
        if (session != null) {
            session.addEventListener(new DebugWorkingMemoryEventListener());
            session.addEventListener(new DebugAgendaEventListener());
            session.addEventListener((WorkingMemoryEventListener) this);
            session.addEventListener((AgendaEventListener) this);
        }
    }

    protected void tearDown() throws Exception {
        if (session != null) {
            session.dispose();
        }
        clear();
        System.out.println("----------");

        super.tearDown();
    }

    protected void clear() {
        firedList.clear();
        insertedList.clear();
        updatedList.clear();
        retractedList.clear();
    }
}
