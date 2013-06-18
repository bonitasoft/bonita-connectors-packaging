package org.bonitasoft.connectors.sugarcrm;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.sugarcrm.SugarCRMUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SugarCRMUtilsTest {
    public static String PASSWORD;
    public static String USERNAME;
    public static String BASE_URL;
    private SugarCRMUtils sugarCRMUtils;
    private String sessionId;
    private static String PREFIX="testRD";

    public SugarCRMUtilsTest() throws IOException {
        Properties prop = new Properties();
        prop.load(this.getClass().getResourceAsStream("/connectors_config.properties"));

        BASE_URL = prop.getProperty("sugarcrm.url");
        USERNAME = prop.getProperty("sugarcrm.username");
        PASSWORD = prop.getProperty("sugarcrm.password");
    }

    @Before
    public void setUp() throws IOException, NoSuchAlgorithmException {
        sugarCRMUtils = new SugarCRMUtils(BASE_URL);
        sessionId = sugarCRMUtils.login(USERNAME, PASSWORD);
    }

    @After
    public void teadDown() throws IOException, NoSuchAlgorithmException {
        if (sessionId == null) {
            sessionId = sugarCRMUtils.login(USERNAME, PASSWORD);
        }

        String objectType = "Accounts";
        String query = "accounts.name LIKE '"+PREFIX+"%'";
        String offset = "0";
        List<String> select_fields = new ArrayList<String>();
        select_fields.add("name");
        select_fields.add("description");
        String max_results = "50";

        JSONObject jsonObject = sugarCRMUtils.query(sessionId, objectType, query, offset, select_fields, max_results);
        JSONArray jsonArray = (JSONArray) jsonObject.get("entry_list");
        for (JSONObject aJsonArray : (List<JSONObject>) jsonArray) {
            sugarCRMUtils.deleteARecord(sessionId, "Accounts", (String) aJsonArray.get("id"));

        }
        logout();
    }

    @Test
    public void emptyListToMap() {
        assertThat(SugarCRMUtils.listToMap(new ArrayList<List<Object>>()).size(), is(0));
    }

    @Test
    public void nullToMap() {
        assertThat(SugarCRMUtils.listToMap(null).size(), is(0));
    }

    @Test
    public void oneEltListToMap() {
        List<Object> line = new ArrayList<Object>();
        line.add("Key");
        line.add("Value");
        Map<String, String> result = SugarCRMUtils.listToMap(Collections.singletonList(line));
        assertThat(result.size(), is(1));
        assertThat(result.get("Key"), is("Value"));
    }

    @Test
    public void testConnect() throws IOException, NoSuchAlgorithmException {
        logout();
        assertThat(sugarCRMUtils.login(USERNAME, PASSWORD).length(), is(not(0)));
    }

    @Test
    public void loginShouldFail() throws IOException, NoSuchAlgorithmException {
        logout();
        assertNull(sugarCRMUtils.login(USERNAME, "admin1"));
    }

    @Test
    public void createRecord() throws IOException, NoSuchAlgorithmException {
        String jsonObject = createAccount(PREFIX+"Test Account").toJSONString();
        assertThat(jsonObject, containsString("Test Account"));
    }

    @Test
    public void updateRecord() throws IOException, NoSuchAlgorithmException {
        String objectId = (String) createAccount(PREFIX+"Test Account").get("id");
        String description = "An updated description";

        Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("description", description);
        JSONObject jsonObject = sugarCRMUtils.updateObject(sessionId, "Accounts", objectId, parameters);
        JSONObject entry_list = (JSONObject) jsonObject.get("entry_list");
        JSONObject jsonDescription = (JSONObject) entry_list.get("description");

        assertThat((String) jsonObject.get("id"), is(objectId));
        assertThat((String) jsonDescription.get("value"), is(description));

    }

    @Test
    public void deleteRecord() throws IOException, NoSuchAlgorithmException {
        String objectId = createAccount("Test account").get("id").toString();

        JSONObject jsonObject = sugarCRMUtils.deleteARecord(sessionId, "Accounts", objectId);
        JSONObject entity_list = (JSONObject) jsonObject.get("entry_list");
        JSONObject deleted = (JSONObject) entity_list.get("deleted");

        assertThat((String) deleted.get("value"), is("1"));
        checkIfObjectDeleted(objectId);
    }

    @Test
    public void retrieveAnObject() throws IOException {
        String accountName = PREFIX+"Test retrieve account";
        String objectId = createAccount(accountName).get("id").toString();

        JSONObject jsonObject = sugarCRMUtils.retrieveAnObject(sessionId, "Accounts", objectId);
        JSONArray entityList = (JSONArray) jsonObject.get("entry_list");
        JSONObject entityParameters = (JSONObject) entityList.get(0);
        String retrieveId = getValueFromKey("id", entityParameters);
        String module_name = getValueFromKey("module_name", entityParameters);

        assertThat(retrieveId, is(objectId));
        assertThat(module_name, is("Accounts"));
    }

    @Test
    public void deleteSeveralObjects() throws IOException {
        List<String> ids = new ArrayList<String>();
        String account1Name = PREFIX+"Account1";
        String account2Name = PREFIX+"Account2";
        String account3Name = PREFIX+"Account3";
        ids.add(createAccount(account1Name).get("id").toString());
        ids.add(createAccount(account2Name).get("id").toString());
        ids.add(createAccount(account3Name).get("id").toString());
        String objectType = "Accounts";

        sugarCRMUtils.deleteRecords(sessionId, objectType, ids);

        for (String id : ids) {
            checkIfObjectDeleted(id);
        }
    }

    @Test
    public void retrieveSeveralObjects() throws IOException {
        String account1Name = PREFIX+"Account1";
        String account2Name = PREFIX+"Account2";
        String account3Name = PREFIX+"Account3";
        List<String> ids = new ArrayList<String>();
        ids.add(createAccount(account1Name).get("id").toString());
        ids.add(createAccount(account2Name).get("id").toString());
        ids.add(createAccount(account3Name).get("id").toString());
        String objectType = "Accounts";

        List<String> fields = new ArrayList<String>();
        fields.add("id");
        fields.add("name");

        JSONObject jsonObject = sugarCRMUtils.retrieveSeveralObjects(sessionId, objectType, ids, fields);

        for (String id : ids) {
            assertThat(jsonObject.toJSONString(), containsString(id));
        }
        assertThat(jsonObject.toJSONString(), containsString(account1Name));
        assertThat(jsonObject.toJSONString(), containsString(account2Name));
        assertThat(jsonObject.toJSONString(), containsString(account3Name));
    }

    @Test
    public void query() throws IOException {
        createAccount(PREFIX+"acc1");
        createAccount(PREFIX+"acc2");
        createAccount(PREFIX+"acc3");
        createAccount(PREFIX+"acc4");
        createAccount(PREFIX+"acc5");
        String objectType = "Accounts";
        String query = "accounts.name LIKE '"+PREFIX+"%'";
        String offset = "0";
        List<String> select_fields = new ArrayList<String>();
        select_fields.add("name");
        select_fields.add("description");
        String max_results = "30";

        JSONObject jsonObject = sugarCRMUtils.query(sessionId, objectType, query, offset, select_fields, max_results);
        JSONArray jsonArray = (JSONArray) jsonObject.get("entry_list");
        assertThat(jsonArray.size(), is(5));

    }

    private void checkIfObjectDeleted(String id) throws IOException {
        JSONObject jsonObject = sugarCRMUtils.retrieveAnObject(sessionId, "Accounts", id);
        JSONArray entityList = (JSONArray) jsonObject.get("entry_list");
        JSONObject entityParameters = (JSONObject) entityList.get(0);
        JSONArray nameValueList = (JSONArray) entityParameters.get("name_value_list");
        JSONObject deletedObject = (JSONObject) nameValueList.get(1);
        String deleted = (String) deletedObject.get("value");
        assertThat(deleted, is("1"));
    }

    private String getValueFromKey(String keyName, JSONObject entityParameters) {
        return (String) entityParameters.get(keyName);
    }

    private JSONObject createAccount(String name) throws IOException {
        Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("name", name);
        return sugarCRMUtils.createARecord(sessionId, "Accounts", parameters);
    }

    private void logout() throws IOException {
        sessionId = sugarCRMUtils.logout(sessionId);
    }
}
