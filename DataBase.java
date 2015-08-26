package who_is_that_champion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataBase {

    String KEY = "?api_key=";

    public class variables {
//variables get from the match for lanes (JUNGLE NOT INCLUDED)

        int creepsperminblue;//cs per min blue top
        int goldperminblue;//gold per min blue top
        boolean matchwinnerblue; //matchwinner top

        String blueidchamp;//blue id champ top
        int creepsperminred; //cs per min red top
        int goldperminred; //gold per min red top
        boolean winnermatchred; //winner match top
        String redidchamp;//red id champ top
    }

    public class calculus {

        double totalgold;
        double ratiogoldblue;
        double ratiogoldred;
        double totalcs;
        double ratiocsblue;
        double ratiocsred;
        double ratiolaneblue;
        double ratiolanered;
        boolean lanewinnerblue;
        boolean lanewinnerred;

        public calculus(variables lane) {
            double totalgold = lane.goldperminblue + lane.goldperminred;
            double ratiogoldblue = lane.goldperminblue / totalgold;
            double ratiogoldred = lane.goldperminred / totalgold;
            double totalcreeps = lane.creepsperminblue + lane.creepsperminred;
            double ratiocreepsblue = lane.creepsperminblue / totalcreeps;
            double ratiocreepsred = lane.creepsperminred / totalcreeps;
            double ratiolaneblue = (ratiocreepsblue + ratiogoldblue) / 2;
            double ratiolanered = (ratiocreepsred + ratiogoldred) / 2;
            boolean lanewinnerblue = false;
            boolean lanewinnerred = false;
            if (ratiolaneblue > ratiolanered) {
                lanewinnerblue = true;
            } else {
                lanewinnerred = true;
            }
        }
    }

    public class lanecontainer {

        JSONArray botblue = new JSONArray();
        JSONArray midblue = new JSONArray();
        JSONArray topblue = new JSONArray();
        JSONArray botred = new JSONArray();
        JSONArray midred = new JSONArray();
        JSONArray topred = new JSONArray();
    }

    public void lane() throws SQLException, IOException, MalformedURLException, ParseException {
        //accesing to mysql server 
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/phpmyadmin", "root", "");
        Statement statement = connection.createStatement();
        //now let's delete all tables for secure that a data doesnt repeat
        statement.execute("delete from apilol.toplane");
        //now let's obtain the list of all black marjet matches by dowloading the json from GitHub.
        URL urlEUWjson = new URL("https://raw.githubusercontent.com/RuetonIrenji/WhoIsThatEnemy/master/EUW.JSON");
        InputStream inputstream = urlEUWjson.openStream();
        InputStreamReader reader = new InputStreamReader(inputstream);
        BufferedReader buffered = new BufferedReader(reader);
        JSONParser parser = new JSONParser();
        JSONArray BlackMatchesJSONArray = (JSONArray) parser.parse(buffered);
        String[] BlackMatches = (String[]) BlackMatchesJSONArray.toArray(new String[10000]);
        //Now we have a JSONArray with all matchId.
        //Now lets go over the all matches with a for
        for (String matchid : BlackMatches) { //go over the all mathces
            //Obtain the match information in a JSON archive.
            //inicialize control variables to get the bot stats:
            URL urlmatch = new URL("https://euw.api.pvp.net/api/lol/euw/v2.2/match/" + matchid + KEY);
            InputStream inputstreammatch = urlmatch.openStream();
            InputStreamReader readermatch = new InputStreamReader(inputstreammatch);
            BufferedReader buffermatch = new BufferedReader(readermatch); //The parser is already created.
            JSONObject match = (JSONObject) parser.parse(buffermatch); //we have the match class.
            //The participant list of the match
            JSONArray participantlist = (JSONArray) match.get("participants");
            //Variables used in the bucle which will be put into the database of TOPLANE:
            //Now let's analizate each participant
           /*The problem here is the champion dont use the actual meta 1 top 1 mid
             1 jungle and 2 bot, this code will collect into arrays the data by lanes, for that we use 
             the JSONLane class*/
            lanecontainer all = new lanecontainer();
            for (int i = 0; i < 10; i++) {
                JSONObject participant = (JSONObject) participantlist.get(i);//participant inf.
                JSONObject statsPerMin = (JSONObject) participant.get("timeline");//participant stats per min
                String LANE = (String) statsPerMin.get("lane"); //Get the lane.
                switch (LANE) {
                    case "TOP"://if top, then return the stats in "top" named object
                        switch ((int) participant.get("teamid")) {
                            case 100://cretes a object depending of the team.
                                variables topblue = this.getMatchStats(participant, statsPerMin);
                                all.topblue.add(topblue);
                                break;
                            case 200:
                                variables topred = this.getMatchStats(participant, statsPerMin);
                                all.topred.add(topred);
                                break;
                        }
                        break;
                    case "MID":
                        switch ((int) participant.get("teamid")) {
                            case 100:
                                variables midblue = this.getMatchStats(participant, statsPerMin);
                                all.midblue.add(midblue);
                                break;
                            case 200:
                                variables midred = this.getMatchStats(participant, statsPerMin);
                                all.midred.add(midred);
                                break;
                        }
                        break;
                    case "BOT":
                        switch ((int) participant.get("teamid")) {//creates four objects in total
                            case 100:
                                variables botblue = this.getMatchStats(participant, statsPerMin);
                                all.botblue.add(botblue);
                                break;
                            case 200:
                                variables botred = this.getMatchStats(participant, statsPerMin);
                                all.botred.add(botred);
                                break;
                        }
                        break;
                    case "BOTTOM"://the same code is repeated because the champion can have one of both values when being at bottom lane
                        switch ((int) participant.get("teamid")) {//creates four objects in total
                            case 100:
                                variables botblue = this.getMatchStats(participant, statsPerMin);
                                all.botblue.add(botblue);
                                break;
                            case 200:
                                variables botred = this.getMatchStats(participant, statsPerMin);
                                all.botred.add(botred);
                                break;
                        }
                        break;
                }
            }
            //now we get the map of all names using the id as key to fill the data base.
        Map<String, String> champbyid = this.champNamebyId();
       
        
            for (Object topblue : all.topblue) {
                //adding top blue lane
                variables lane = (variables) topblue;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champid,enemychampid,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.blueidchamp+"','"+lane.redidchamp+"','"+lane.goldperminblue+"',"
                        + "'"+lane.matchwinnerblue+"','"+lanecalculus.ratiogoldblue+"','"+lanecalculus.ratiocsblue+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolaneblue+"','"+lanecalculus.lanewinnerblue+"','"+champbyid.get(lane.blueidchamp)+"','"+champbyid.get(lane.redidchamp)+"'");
            }
            for (Object topred : all.topred) {
                //adding top red lane
                variables lane = (variables) topred;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champid,enemychampid,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.redidchamp+"','"+lane.blueidchamp+"','"+lane.goldperminred+"',"
                        + "'"+lane.winnermatchred+"','"+lanecalculus.ratiogoldred+"','"+lanecalculus.ratiocsred+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolanered+"','"+lanecalculus.lanewinnerred+"','"+champbyid.get(lane.redidchamp)+"','"+champbyid.get(lane.blueidchamp)+"'");
            }
            for (Object midblue : all.midblue) {
                //adding mid blue lane
                variables lane = (variables) midblue;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champid,enemychampid,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.blueidchamp+"','"+lane.redidchamp+"','"+lane.goldperminblue+"',"
                        + "'"+lane.matchwinnerblue+"','"+lanecalculus.ratiogoldblue+"','"+lanecalculus.ratiocsblue+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolaneblue+"','"+lanecalculus.lanewinnerblue+"','"+champbyid.get(lane.blueidchamp)+"','"+champbyid.get(lane.redidchamp)+"'");
            }
             for (Object midred : all.midred) {
                //adding mid red lane
                variables lane = (variables) midred;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champid,enemychampid,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.redidchamp+"','"+lane.blueidchamp+"','"+lane.goldperminred+"',"
                        + "'"+lane.winnermatchred+"','"+lanecalculus.ratiogoldred+"','"+lanecalculus.ratiocsred+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolanered+"','"+lanecalculus.lanewinnerred+"','"+champbyid.get(lane.redidchamp)+"','"+champbyid.get(lane.blueidchamp)+"'");
            }
             for (Object botblue : all.botblue) {
                //adding bot blue lane
                variables lane = (variables) botblue;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champid,enemychampid,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.blueidchamp+"','"+lane.redidchamp+"','"+lane.goldperminblue+"',"
                        + "'"+lane.matchwinnerblue+"','"+lanecalculus.ratiogoldblue+"','"+lanecalculus.ratiocsblue+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolaneblue+"','"+lanecalculus.lanewinnerblue+"','"+champbyid.get(lane.blueidchamp)+"','"+champbyid.get(lane.redidchamp)+"'");
            }
               for (Object botred : all.botred) {
                //adding bot red lane
                variables lane = (variables) botred;
                calculus lanecalculus=new calculus(lane);
                statement.execute("insert into apilol.toplane(matchid,champ,enemychamp,gold,creeps,matchwinner,ratiogold,ratiocreeps,totalgold,"
                        + "totalcreeps,totalratio, lanewinner,champ, enemychamp) values ('"+matchid+"','"+lane.redidchamp+"','"+lane.blueidchamp+"','"+lane.goldperminred+"',"
                        + "'"+lane.winnermatchred+"','"+lanecalculus.ratiogoldred+"','"+lanecalculus.ratiocsred+"','"+lanecalculus.totalgold+"',"
                        + "'"+lanecalculus.totalcs+"','"+lanecalculus.ratiolanered+"','"+lanecalculus.lanewinnerred+"','"+champbyid.get(lane.redidchamp)+"','"+champbyid.get(lane.blueidchamp)+"'");
            }
        }
        statement.close();
        connection.close();
    }

    public Map champMap() throws MalformedURLException, IOException, ParseException {
        URL url = new URL("https://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion" + KEY);
        InputStream inputstream = url.openStream();
        InputStreamReader reader = new InputStreamReader(inputstream);
        BufferedReader buffer = new BufferedReader(reader);
        JSONParser parser = new JSONParser();
        JSONObject champData = (JSONObject) parser.parse(buffer);
        Map<String, JSONObject> champDataMap = (JSONObject) champData.get("data");
        return champDataMap;
    }

    public Map champNamebyId() throws IOException, MalformedURLException, ParseException {
        Map<String, JSONObject> champDataMap = this.champMap();
        int numChamp = champDataMap.size();
        Set<String> champNamesSet = champDataMap.keySet();
        String[] champNames = champNamesSet.toArray(new String[champNamesSet.size()]);
        Map<String, String> champIdMap = new HashMap<String, String>();
        for (int i = 0; i < numChamp; i++) {
            JSONObject championDto = (JSONObject) champDataMap.get(champNames[i]);
            champIdMap.put(championDto.get("id").toString(), champNames[i]);
        }
        return champIdMap;
    }

    public variables getMatchStats(JSONObject participant, JSONObject statsPerMin) {
        JSONObject Stats = (JSONObject) participant.get("Stats");//participant stats

        variables top = new variables(); //its named top because the moment of this code building.
        switch ((int) participant.get("teamid")) {
            case 100:
                //here we obtain the values from JSON and we fill the variables.
                JSONObject creepsPerMinDeltasBlue = (JSONObject) statsPerMin.get("creepsPerMinDeltas");
                JSONObject goldPerMinDeltasBlue = (JSONObject) statsPerMin.get("goldPerMinDeltas");
                //the variables which fill the data base
                if ((boolean) Stats.get("winner")) {
                    top.matchwinnerblue = true;
                }
                top.creepsperminblue = (int) creepsPerMinDeltasBlue.get("zerototen");
                top.goldperminblue = (int) goldPerMinDeltasBlue.get("zerototen");
                top.blueidchamp = participant.get("championID").toString();
                break;
            case 200:
                JSONObject creepsPerMinDeltasRed = (JSONObject) statsPerMin.get("creepsPerMinDeltas");
                JSONObject goldPerMinDeltasRed = (JSONObject) statsPerMin.get("goldPerMinDeltas");
                //the variables which fill the data base

                if ((boolean) Stats.get("winner")) {
                    top.winnermatchred = true;
                }
                top.creepsperminred = (int) creepsPerMinDeltasRed.get("zerototen");
                top.goldperminred = (int) goldPerMinDeltasRed.get("zerototen");
                top.redidchamp = participant.get("championID").toString();
                break;
        }

        return top;
    }
}
