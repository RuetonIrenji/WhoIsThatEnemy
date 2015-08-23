package who_is_that_champion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DatabaseCreator {
String KEY="?api_key=";
    public JSONObject CreateDatabasebyLane() throws IOException, ParseException {
        //obtaining Names
        Map<String,JSONObject> champDataMap=this.champMap();//ChampMap() is defined below
        Set<String> champKeysSet=champDataMap.keySet();
        String[] champKeys=champKeysSet.toArray(new String[champDataMap.size()]);
        //Creating lanes objects.
        JSONObject Lane= new JSONObject();
        
        for(int i=0;i<champDataMap.size();i++){
            JSONObject champDto1=(JSONObject) champDataMap.get(champKeys[i]);
            String champId1= champDto1.get("id").toString();
            JSONObject Champs= new JSONObject();
            for(int j=0;j<champDataMap.size();j++){
                if(j==i){continue;}
                //get the id. Number 2 is related to enemy champ
                JSONObject champDto2=(JSONObject) champDataMap.get(champKeys[j]);
                String champId2= champDto2.get("id").toString();
                JSONObject enemyChamps= new JSONObject();
                 JSONArray money= new JSONArray();
                JSONArray cs= new JSONArray();                
                enemyChamps.put("money per min", money);
                enemyChamps.put("cs per min", cs);
                //add here the new values:
                //example: 
                //JSONArray example= new JSONArray;
                //enemyChamps.put("example",example); Remember to change the filler code too.
                
                
                //
                Champs.put(champId2, enemyChamps);  
                Lane.put(champId1,Champs);
                //when refill the database enemyChamps.put(stat,value);
            }
        }
       return Lane; 
    }/*Bot lane may require a new data base*/
public Map champMap() throws MalformedURLException, IOException, ParseException{
    URL url = new URL("http://global.api.pvp.net/api/lol/static-data/euw/v1.2/champion"+ KEY);
        InputStream inputstream = url.openStream();
        InputStreamReader reader = new InputStreamReader(inputstream);
        BufferedReader buffer = new BufferedReader(reader);
        JSONParser parser = new JSONParser();
        JSONObject champData = (JSONObject) parser.parse(buffer);
        Map<String,JSONObject> champDataMap=(JSONObject) champData.get("Data");
    return champDataMap;
    }
public Map champNamebyId() throws IOException, MalformedURLException, ParseException{
    Map<String,JSONObject> champDataMap= this.champMap();
   int numChamp= champDataMap.size();
   Set<String> champNamesSet=champDataMap.keySet();
   String[] champNames= champNamesSet.toArray(new String[champNamesSet.size()]);
   Map<String,String> champIdMap=null;
   for(int i=0;i<numChamp;i++){
     JSONObject championDto=(JSONObject) champDataMap.get(champNames[i]);
       champIdMap.put(championDto.get("id").toString(), champNames[i].toString());
           }
   return champIdMap;
}
public JSONObject createDatabaseJungle() throws IOException, MalformedURLException, ParseException{
Map<String,JSONObject> champDataMap=this.champMap();
        Set<String> champKeysSet=champDataMap.keySet();
        String[] champKeys=champKeysSet.toArray(new String[champDataMap.size()]);
        JSONObject jungleLane= new JSONObject();
        JSONObject champs= new JSONObject();
        JSONArray money= new JSONArray();
        JSONArray cs= new JSONArray();  
        champs.put("money per min", money);
        champs.put("cs per min", cs);
           //add here the new values:
                //example: 
                //JSONArray example= new JSONArray;
                //enemyChamps.put("example",example); Remember to change the filler code too.
                
                
                //
        for(int i=0;i<champDataMap.size();i++){
        String champName= champKeys[i];
        jungleLane.put(champName, champs);
                }
        return jungleLane;
        
}
}

