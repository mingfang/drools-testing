package sample;

import com.rebelsoft.testing.drools.DroolsTestCase;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 2/27/13
 * Time: 9:42 PM
 */
public class SampleDroolsTest extends DroolsTestCase {

    private final KnowledgeBase kbase;

    public SampleDroolsTest() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newClassPathResource("sample.drl", getClass()), ResourceType.DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( errors.size() > 0 ) {
            for ( KnowledgeBuilderError error : errors ) {
                System.err.println( error.toString() );
            }
            throw new IllegalArgumentException( "Could not parse knowledge." );
        }
        assertFalse( kbuilder.hasErrors() );

        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

    }

    @Override
    protected StatefulKnowledgeSession setUpSession() {
        return kbase.newStatefulKnowledgeSession();
    }

    public void testOne() throws Exception {
        insertAndFire(1);
        assertFactCount(1);
        assertFiredRule("One");
    }

    public void testOneTwoThree() throws Exception {
        insertAndFire(1);
        insertAndFire(2);
        insertAndFire(3);
        assertFactCount(3);
        assertFiredRulesInOrder("One", "Two", "Three");
    }

}
