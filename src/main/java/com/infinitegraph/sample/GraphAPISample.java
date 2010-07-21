package com.infinitegraph.sample;

// All the import packages
import com.objy.graph.*;
import com.objy.graph.navigation.*;

/*
 * In this example, you will see a simple example of adding vertices and edges to 
 * an Infinite Graph Database. We then use these vertices to traverse through the 
 * Graph.
 */

public class GraphAPISample
{

    public static void main(String[] args)
    { 
    	Transaction tx = null;
    	GraphDatabase myGraphDB = null;
        
        try
        {
        	// Create a Graph Database first
        	GraphFactory.create("SampleGraph", "SampleGraphProperties.properties");
        	// Open the database, specifying name and the location of a
            // property file which contains the database descriptor
            myGraphDB = GraphFactory.open("SampleGraph", "SampleGraphProperties.properties");

            // Start a transaction
            tx = myGraphDB.beginTransaction(AccessMode.READ_WRITE);
            
            // Create the three people as vertices and "meeting" edges
            Person suspect1 = new Person("Tom Riddle");
            Person suspect2 = new Person("Bellatrix Lestrange");
            Person unknown1 = new Person("Nancy Smith"); 
            
            MetWith meeting1 = new MetWith("Borgin and Burkes");
            MetWith meeting2 = new MetWith("Leaky Cauldron");
       
            // Add three vertices to graph
            myGraphDB.addVertex(suspect1);
            myGraphDB.addVertex(unknown1);
            myGraphDB.addVertex(suspect2);

            // Connects the 3 vertices:
            // suspect1 <-meeting1-> unknown1 <-meeting2-> suspect2
            suspect1.addEdge(meeting1, unknown1, EdgeKind.BIDIRECTIONAL);
            unknown1.addEdge(meeting2, suspect2, EdgeKind.BIDIRECTIONAL);
            
            // Name the Vertices for search capability
            myGraphDB.nameVertex("suspect1", suspect1);
            myGraphDB.nameVertex("suspect2", suspect2);

            // Get the vertex by the name given
            Person retrievedSuspect1 = (Person)myGraphDB.getNamedVertex("suspect1");
            
            // See if we retrieved the right person.
            if (retrievedSuspect1.getName().equals(suspect1.getName()))
            {
            	System.out.println("Found " + retrievedSuspect1.getName()); 
            }
            
            // Commit the graph additions, downgrading to read
            tx.checkpoint(true);


            //instantiate the qualifier and result handler we will be using during navigation.
            PrintPathResultsHandler resultPrinter = new PrintPathResultsHandler();
            meetingWithBellatrix meetingWithBellatrix1 = new meetingWithBellatrix();

            // Start a breadth first navigation from Chopper looking for this meeting
            // Qualifier.ANY  - instruct the navigate to pick any path to go down when it is traversing from the node
            // meetingWithBellatrix1 - instruct the navigate to only take paths with ending vertex "Bellatrix Lestrange"
            // resultPrinter - After a path went through both qualifiers, it will print out the name of the vertices and the edges
            //                 that connect them.
            Navigator suspect1Navigator = suspect1.navigate(Guide.SIMPLE_BREADTH_FIRST, Qualifier.ANY, meetingWithBellatrix1, resultPrinter);
            suspect1Navigator.start();
            suspect1Navigator.stop();

            tx.commit();
            
            //Delete the Database from the System
	    System.out.println();
	    System.out.println("Sample Database removed");
            GraphFactory.delete("SampleGraph", "SampleGraphProperties.properties");
	    }
        catch (ConfigurationException cE){
        	System.out.println(" Configuration Exception was thrown .. ");
        	System.out.println(cE.getMessage());
        }
	    finally
	    {
	       // If the transaction was not committed, complete
	       // will roll it back
	    	if (tx != null)
	    		tx.complete();
	        if (myGraphDB != null)
	        	myGraphDB.close();
	    }
    }
}

/*
 * For every result passing through all the qualifiers in the navigation,
 * this will print out all the possible paths.
 */
class PrintPathResultsHandler implements NavigationResultHandler
{
	public void handleResultPath(Path result, Navigator navigator) {
		System.out.print("Found matching path : ");
		System.out.print(result.get(0).getVertex().toString());
		
		// For h in p
        for(Hop h : result)
        {
            if(h.hasEdge())
            {
                System.out.print("<->" + h.getVertex().toString());
            } 
        }
	}
};

/*
 * This qualifier only return true if the end of the path is "Bellatrix Lestrange"
 */
class meetingWithBellatrix implements Qualifier
{
	public boolean qualify(Path currentPath)
	{
		//this is a qualifying path if the last vertex of this path is Bellatrix Lestrange
		if ("Bellatrix Lestrange" == ((Person)(currentPath.getFinalHop().getVertex())).getName())
			return true;
		else
			return false;
	}
};

/*
* The persistent class definition for the Person Vertex type
*/
class Person extends BaseVertex
{
    private String name;

    public Person(String name)
    {
        setName(name);
    }

    public String getName() 
    {
        fetch();
        return this.name;
    }

    public void setName(String name) 
    { 
        markModified();
        this.name = name; 
    } 

@Override
    public String toString()
    {
        return this.name;
    }

};

/*
* The persistent class definition for the MetWith Edge type
*/
class MetWith extends BaseEdge
{
	private String location;

    public MetWith(String location)
    {
        setLocation(location);
    }

    public String getLocation() 
    {
        fetch();
        return this.location;
     }

    public void setLocation(String location) 
    { 
        markModified();
        this.location = location; 
    } 

    public String getLabel()
    {
        return "Met at " + location;
    } 
};