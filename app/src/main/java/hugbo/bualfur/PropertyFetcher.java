package hugbo.bualfur;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by stefan on 18/03/17.
 */

public class PropertyFetcher {
    private final String TAG = "PropertyFetcher";
    private static PropertyFetcher mPropertyFetcherInstance;
    private ArrayList<Property> mProperties;
    private String developmentURL = "http://192.168.0.101:3000/properties/search";
    private String productionURL = "https://hugbo-verkefni1-dev.herokuapp.com/properties/search";
    private Context mCtx;

    private PropertyFetcher(Context context){
            mCtx = context;
    }


    public ArrayList<Property> getProperties(){
        return mProperties;
    }


    public JSONObject defaultParameters(){
        HashMap<String, String> searchParams = new HashMap<>();

        searchParams.put("price_max", "");
        searchParams.put("price_min", "");
        searchParams.put("property_type", "");
        searchParams.put("rooms_max", "");
        searchParams.put("rooms_min", "");
        searchParams.put("square_meters_max", "0");
        searchParams.put("square_meters_min", "0");
        searchParams.put("zipcode", "");


        JSONObject data = new JSONObject(searchParams);

        HashMap<String, JSONObject> tmp = new HashMap<String, JSONObject>();
        tmp.put("search", data);
        data = new JSONObject(tmp);

        return data;
    }

    /**
     * Search method that fetches a list of properties from the hugbo1 server
     * @param data the JSONObject that contains the search parameters
     * @param callback success callback to return the results
     */
    public void searchProperties(JSONObject data, final ServerCallback callback){
        mProperties = new ArrayList<Property>();

        if(data == null){
            // Use dummy properties
            Property tmpProp = new Property("1","Smyrlahraun 50", 201, "Hafnarfjörður", 60, 60, 500, 50, 1, 1, "Einbýlishús");
            mProperties.add(tmpProp);
            tmpProp = new Property("2","Gunnlaugsstræti 33", 105, "Reykjavík", 60, 60, 800, 70, 2, 1, "Einbýlishús");
            mProperties.add(tmpProp);
            tmpProp = new Property("3","Gerðargata 12", 101, "Reykjavík", 60, 60, 300, 40, 1, 2, "Fjölbýlishús");
            mProperties.add(tmpProp);

            callback.onSuccess(mProperties);
        }

        //        Log.i(TAG, "searchProperties: "+data.toString());


        JsonObjectRequest requestObject = new JsonObjectRequest(Request.Method.POST, productionURL, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    JSONArray propertiesArray = response.getJSONArray("properties");
//                    Log.i(TAG, propertiesArray.toString());

                    for (int i = 0; i < propertiesArray.length(); i++) {
                        JSONObject property = propertiesArray.getJSONObject(i);
//                        Log.i(TAG, property.toString());

                        String id = property.getString("property_id");

                        String address = property.getString("address");


                        int zipcode = property.getInt("zipcode");

                        String city = property.getString("city");


                        int price = property.getInt("price");


                        int size = property.getInt("size");

                        String propertyType = property.getString("property_type");

                        int numBedrooms = property.getInt("rooms");


                        int numBathrooms = property.getInt("bathrooms");


                        JSONObject gpsLocation = property.getJSONObject("gpslocation");

                        double lat = gpsLocation.getDouble("lat");

                        double lng = gpsLocation.getDouble("lng");

                        Property tmpProperty = new Property(id, address, zipcode, city, price, size, lat, lng, numBedrooms, numBathrooms, propertyType);
                        mProperties.add(tmpProperty);

                    }
                    callback.onSuccess(mProperties);
                } catch (JSONException je){
                    Log.e(TAG, je.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PropertyStorage", error.toString());
            }
        });



        NetworkController.getInstance(mCtx.getApplicationContext()).addToRequestQueue(requestObject);
    }

    public Property getProperty(UUID id) {

        for (Property property : mProperties){
            if(property.getmId().equals(id)){
                return property;
            }
        }

        return null;

    }


    public static synchronized PropertyFetcher getInstance(Context context){
        if(mPropertyFetcherInstance == null){
            mPropertyFetcherInstance = new PropertyFetcher(context);
        }
        return mPropertyFetcherInstance;
    }
}
