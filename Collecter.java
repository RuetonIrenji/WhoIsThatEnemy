/*This class will collect all data necessary from delevopers API to the JSON database*/
//This class will be only used to get the data base and its possible its not be included into the program code
package who_is_that_champion;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Collecter {
    String KEY="?api_key=";
   public JSONObject ObtainALane(String LANE) throws MalformedURLException, IOException, ParseException{
       //NOTE: there is a var named toplane, but its used for midlane too.
       //NOTE: only for top and mid lane
if(LANE!="TOP" && LANE!="MID"){System.out.print("Input values: 'MID' and 'TOP' ");return null;}else{   
//Obtain matchID from GITHUB archive.
   URL urlEUWjson= new URL("https://raw.githubusercontent.com/RuetonIrenji/WhoIsThatEnemy/master/EUW.JSON");
   InputStream inputstream=urlEUWjson.openStream();
   InputStreamReader reader= new InputStreamReader(inputstream);
   BufferedReader buffered= new BufferedReader(reader);
   JSONParser parser= new JSONParser();
   JSONArray BlackMatches=(JSONArray) parser.parse(buffered);
  //Create the data base.
   DatabaseCreator laneobtainer= new DatabaseCreator();
   JSONObject toplane= laneobtainer.CreateDatabasebyLane();
   JSONObject midlane= laneobtainer.CreateDatabasebyLane();
   //acces to all matches and collect the necessary information
           for(Object matchidObject: BlackMatches.toArray()){
              String matchid=matchidObject.toString();
              //acces to class MatchDetail from api developers.
              URL urlmatch= new URL("https://euw.api.pvp.net/api/lol/euw/v2.2/match/"+matchid+KEY);
              InputStream inputstreammatch= urlmatch.openStream();
              InputStreamReader readermatch= new InputStreamReader(inputstreammatch);
              BufferedReader buffermatch= new BufferedReader(readermatch); //The parser is already created.
              JSONObject match=(JSONObject)parser.parse(buffermatch); //we have the match class.
              //Now lets collect the data.
              JSONArray participantlist=(JSONArray) match.get("participants");
                            int creepsPerMinBlue=0;
                            int goldPerMinBlue=0;
                            String blueid=null;  
                            int creepsPerMinRed=0;
                            int goldPerMinRed=0;
                            String redid=null;
              for(int i=0;i<10;i++){
                    JSONObject participant= (JSONObject) participantlist.get(i);
                    JSONObject Stats=(JSONObject)participant.get("Stats");
                    JSONObject statsPerMin=(JSONObject)participant.get("timeline");
                   if(Stats.get("lane").toString()==LANE){
                            switch((int)participant.get("teamid")){
                                case 100:
                            JSONObject creepsPerMinDeltasBlue=(JSONObject) statsPerMin.get("creepsPerMinDeltas");
                            JSONObject goldPerMinDeltasBlue=(JSONObject) statsPerMin.get("goldPerMinDeltas");
                           //the variables which fill the data base
                            creepsPerMinBlue= (int) creepsPerMinDeltasBlue.get("zerototen");
                             goldPerMinBlue=(int) goldPerMinDeltasBlue.get("zerototen");
                            blueid= participant.get("championID").toString();
                                    break;
                                case 200:
                                JSONObject creepsPerMinDeltasRed=(JSONObject) statsPerMin.get("creepsPerMinDeltas");
                            JSONObject goldPerMinDeltasRed=(JSONObject) statsPerMin.get("goldPerMinDeltas");
                           //the variables which fill the data base
                            creepsPerMinRed= (int) creepsPerMinDeltasRed.get("zerototen");
                            goldPerMinRed=(int) goldPerMinDeltasRed.get("zerototen");
                            redid= participant.get("championID").toString();
                                   break;
                            }
                    }
                }
              //add the data to the database of champs
              JSONObject champBlue=(JSONObject) toplane.get(blueid);
              JSONObject enemyBlue=(JSONObject) champBlue.get(redid);
              JSONArray moneyBlue= (JSONArray) enemyBlue.get("money per min");
              JSONArray csBlue=(JSONArray) enemyBlue.get("cs per min");
              moneyBlue.add(goldPerMinBlue);
              csBlue.add(creepsPerMinBlue);
              enemyBlue.put("money per min",moneyBlue);
              enemyBlue.put("cs per min",csBlue);
              champBlue.put(redid,enemyBlue); 
              toplane.put(blueid,champBlue);
              //here we are getting the array which we want to fill and then we put again into the JSONObject.
              JSONObject champRed=(JSONObject) toplane.get(redid);
              JSONObject enemyRed=(JSONObject) champRed.get(blueid);
              JSONArray moneyRed= (JSONArray) enemyRed.get("money per min");
              JSONArray csRed=(JSONArray) enemyRed.get("cs per min");
              moneyBlue.add(goldPerMinRed);
              csBlue.add(creepsPerMinRed);
              enemyBlue.put("money per min",moneyRed);
              enemyBlue.put("cs per min",csRed);
              champBlue.put(blueid,enemyRed); 
              toplane.put(redid,champRed);
              
           }
       return toplane;
               }
   }
}
