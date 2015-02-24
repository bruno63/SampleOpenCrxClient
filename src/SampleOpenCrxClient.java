import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;

import org.opencrx.kernel.account1.cci2.ContactQuery;
import org.opencrx.kernel.account1.cci2.GroupQuery;
import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.kernel.account1.jmi1.Contact;
import org.opencrx.kernel.account1.jmi1.Group;
import org.opencrx.kernel.account1.jmi1.Segment;
import org.opencrx.kernel.activity1.cci2.ActivityQuery;
import org.opencrx.kernel.activity1.cci2.ActivityTrackerQuery;
import org.opencrx.kernel.activity1.jmi1.Activity;
import org.opencrx.kernel.activity1.jmi1.ActivityTracker;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Sample openCRX client program.
 *
 */
public class SampleOpenCrxClient {
	private static final String CONNECTION_URL = "http://demo.opencrx.org/opencrx-rest-CRX/";
	private static final String USER_NAME = "guest";
	private static final String PASSWORD = "guest";
	private static final String PROVIDER = "CRX";
	private static final String SEGMENT = "Standard";
	private String XRI_account1 = "xri://@openmdx*org.opencrx.kernel.account1/provider/"
			+ PROVIDER + "/segment/" + SEGMENT;
	private String XRI_activity1 = "xri://@openmdx*org.opencrx.kernel.activity1/provider/"
			+ PROVIDER + "/segment/" + SEGMENT;
	private static final short MAX_RECS = 100;
	private static final boolean FILTERED_BY_FULLNAME = true;
	private PersistenceManager pm = null;

	// TODO: do the same with a JDO mongodb and JDO JSON
	// id in opencrx: XRI, in mongodb: _id, in JSON: _id

	public SampleOpenCrxClient() {
		/*
		 * String connectionUrl = "http://127.0.0.1:8080/opencrx-rest-CRX/";
		 * String userName = "admin-Standard"; String password =
		 * "admin-Standard";
		 */
		try {
			System.out.println("**************** connecting to openCrx("
					+ CONNECTION_URL + ", " + USER_NAME + "/" + PASSWORD);

			// application/vnd.openmdx.wbxml | text/xml | application/json
			PersistenceManagerFactory _pmf = org.opencrx.kernel.utils.Utils
					.getPersistenceManagerFactoryProxy(CONNECTION_URL,
							USER_NAME, PASSWORD,
							"application/vnd.openmdx.wbxml");
			pm = _pmf.getPersistenceManager(USER_NAME, null);
		} catch (ServiceException ex) {
			System.out.println("*** ServiceException: " + ex.toString());
			System.exit(-1);
		} catch (NamingException ex) {
			System.out.println("*** NamingException: " + ex.toString());
			System.exit(-1);
		}
	}

	public void close() {
		pm.close();
	}

	public void listContacts(String searchTerm) {
		System.out.println("**************** listContacts("
				+ (searchTerm == null ? "null" : searchTerm) + ")");
		Segment _segment = (Segment) pm.getObjectById(new Path(XRI_account1));
		ContactQuery _query = (ContactQuery) pm.newQuery(Contact.class);
		_query.orderByFullName().ascending();
		if (FILTERED_BY_FULLNAME) {
			if (searchTerm != null) {
				// thereExistsFullName() returns
				// org.w3c.cci2.StringTypePredicate. How can this be chained
				// with like() ?
				_query.thereExistsFullName().like(searchTerm);
			} else {
				_query.thereExistsFullName();
			}
		}
		int _count = 0;

		// TODO: why are there XRI's in special format:
		// daa6f456-d4f0-11e2-95d2-dd9cebe030de
		List<Contact> _results = _segment.<Contact> getAccount(_query);
		System.out.println("found " + _results.size()
				+ " Contacts, only the first " + MAX_RECS + " are shown:");

		for (Contact _result : _results) {
			System.out.println("[" + _count + "] "
					+ _result.refGetPath().toXRI() + ": " + "<"
					+ _result.getFullName() + ">");
			_count++;
			if (_count > MAX_RECS) {
				break;
			}
		}
	}

	public void listAddressbooks() {
		System.out.println("**************** listAddressbooks()");
		Segment _segment = (Segment) pm.getObjectById(new Path(XRI_account1));
		GroupQuery _query = (GroupQuery) pm.newQuery(Group.class);
		_query.orderByFullName().ascending();

		// filter for AddressBooks
		// Addressbook = Group with accountType=100
		// Addressbook entries may be of Type Contact or LegalEntity
		// 1:n Addressbooks per Tenant are possible
		_query.thereExistsAccountType().elementOf(100);
		int _count = 0;

		List<Group> _results = _segment.<Group> getAccount(_query);
		System.out.println("found " + _results.size()
				+ " Addressbooks, only the first " + MAX_RECS + " are shown:");

		for (Group _result : _results) {
			System.out.println("[" + _count + "] "
					+ _result.refGetPath().toXRI() + ": " + "<"
					+ _result.getFullName() + ">");
			_count++;
			if (_count > MAX_RECS) {
				break;
			}
		}
	}

	public String createAddressbook(String name) {
		System.out.println("**************** createAddressbook(" + name + ")");
		Segment _segment = (Segment) pm.getObjectById(new Path(XRI_account1));
		GroupQuery _query = (GroupQuery) pm.newQuery(Group.class);
		// filter for AddressBooks
		// Addressbook = Group with accountType=100
		// Addressbook entries may be of Type Contact or LegalEntity
		// 1:n Addressbooks per Tenant are possible
		_query.thereExistsAccountType().elementOf(100);
		int _count = 0;

		List<Group> _results = _segment.<Group> getAccount(_query);
		System.out.println("found " + _results.size() + " Addressbooks");

		for (Group _result : _results) {
			System.out.println("[" + _count + "] "
					+ _result.refGetPath().toXRI() + ": " + "<"
					+ _result.getFullName() + ">");
			_count++;
		}
		return "";
	}

	public List<ActivityTracker> get(Account account) {
		final short ACTIVITY_GROUP_TYPE_PROJECT = 40;
		final short ACCOUNT_ROLE_CUSTOMER = 100;

		System.out.println("**************** listCustomerProjectGroup()");
		org.opencrx.kernel.activity1.jmi1.Segment _segment = (org.opencrx.kernel.activity1.jmi1.Segment) pm
				.getObjectById(new Path(XRI_activity1));
		ActivityTrackerQuery _query = (ActivityTrackerQuery) pm
				.newQuery(ActivityTracker.class);
		_query.forAllDisabled().isFalse();
		// filter for projects (ActivityGroupType = 40)
		_query.activityGroupType().equalTo(ACTIVITY_GROUP_TYPE_PROJECT);
		// filter for LegalEntity (Assigned Account with accountRole=100)
		_query.thereExistsAssignedAccount().accountRole()
				.equalTo(ACCOUNT_ROLE_CUSTOMER);
		if (account != null) {
			_query.thereExistsAssignedAccount().thereExistsAccount()
					.equalTo(account);
		}
		_query.orderByName().ascending();

		return _segment.getActivityTracker(_query);
	}

	public void printCustomerProjectGroups(
			List<ActivityTracker> customerProjectGroups) {
		if (customerProjectGroups.size() > MAX_RECS) {
			System.out.println("found " + customerProjectGroups.size()
					+ " ActivityTrackers, only the first " + MAX_RECS
					+ " are shown:");
		} else {
			System.out.println("found " + customerProjectGroups.size()
					+ " ActivityTrackers (CustomerProjectGroups)");
		}

		int _count = 0;
		for (ActivityTracker _result : customerProjectGroups) {
			System.out.println("[" + _count + "] "
					+ _result.refGetPath().toXRI() + ": " + "<"
					+ _result.getName() + ">");
			// TODO: extract the hierarchy based on prefixes [nnn.nnn.nnn]
			_count++;
			if (_count > MAX_RECS) {
				break;
			}
		}
	}

	public void printActivities(List<Activity> activities, String groupName) {
		if (activities.size() > MAX_RECS) {
			System.out
					.println("found " + activities.size()
							+ " Activities, only the first " + MAX_RECS
							+ " are shown:");
		} else {
			System.out
					.println("found " + activities.size()
							+ " Activities in CustomerProjectGroup <"
							+ groupName + ">");
		}

		for (Activity _activity : activities) {
			System.out.println("\t<" + _activity.getName() + ">");
		}

	}

	public List<Activity> get(ActivityTracker customerProjectGroup) {
		PersistenceManager _pm = JDOHelper
				.getPersistenceManager(customerProjectGroup);
		ActivityQuery _query = (ActivityQuery) _pm.newQuery(Activity.class);
		_query.forAllDisabled().isFalse();
		_query.orderByName().ascending();
		return customerProjectGroup.getFilteredActivity(_query);
	}

	public static void main(String[] args) {
		SampleOpenCrxClient _client = new SampleOpenCrxClient();
		// _client.listContacts(null);
		// _client.getContacts("123*");
		// TODO: createContact(Contact)
		// TODO: readContact(id)
		// TODO: updateContact(id, Contact)
		// TODO: deleteContact(id)
		// ------------------------------
		_client.listAddressbooks();
		// TODO: get all Contacts per Addressbook (incl. iterations over
		// batches)
		// String _id = _client.createAddressbook("*TEST001*");
		// TODO: readAddressbook(id)
		// TODO: updateAddressbook(id, Addressbook)
		// TODO: deleteAddressbook(id)
		// TODO: createContact(aid, Contact)
		// TODO: readContact(id)
		// TODO: updateContact(aid, id, Contact)
		// TODO: deleteContact(id)
		// TODO: same for Address
		// ------------------------------
		List<ActivityTracker> _results = _client.get((Account) null);
		_client.printCustomerProjectGroups(_results);
		for (int i = 0; i < _results.size(); i++) {
			List<Activity> _activities = _client.get(_results.get(i));
			_client.printActivities(_activities, _results.get(i).getName());
		}
		// TODO: createProject(Project)
		// TODO: readProject(id)
		// TODO: updateProject(id, Project)
		// TODO: deleteProject(id)
	}

}