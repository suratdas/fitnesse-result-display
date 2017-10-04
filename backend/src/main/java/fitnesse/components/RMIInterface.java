package fitnesse.components;

import java.rmi.Remote;
import java.rmi.RemoteException;

//It is important to run rmiregistry from the base of this package
public interface RMIInterface extends Remote {
	  public String publish(String name) throws RemoteException;
	}
