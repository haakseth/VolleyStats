package no.haakseth.volleystats;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
//import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View.OnClickListener;


/**
 * IDEAS:
 * - Make activity for editing stat for player, on contextmenu 
 * */

/**
 * @author      John Wika Haakseth <johnwika@gmail.com>
 * @since       2012-01-30         
 * The application's third activity. Shows a list the team's players
 * to the left, and Buttons for editing stats to the right.
 * 
 * Selecting (clicking/pressing) on a player will select him/her for editing stats.
 * Long-pressing will open a Context-Menu where the player can be renamed or deleted.
 * 
 * */
public class GameActivity extends ListActivity {
	private ListView playerList;
	private CursorAdapter dataSource;
	final DBHelper db = new DBHelper(this);
	private static final String fields[] = { DBHelper.PLAYERNAME, DBHelper.PLAYERID };

    private static final String TAG = "MEDIA";

    int selectedTeamID = -1;
    int selectedGameID = -1;

    int selectedPlayerID = -1;
	TextView statsTextView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game); 
        statsTextView = (TextView)findViewById(R.id.statsTextView);
        Intent gameIntent = getIntent();
        selectedTeamID = gameIntent.getIntExtra("selectedTeamID", -1);
        selectedGameID = gameIntent.getIntExtra("selectedGameID", -1);
        selectedPlayerID = gameIntent.getIntExtra("selectedPlayerID", -1);
        
        
        System.out.println("received selectedTeamID is " + selectedTeamID);
        System.out.println("received selectedGameID is " + selectedGameID);
        System.out.println("received selectedPlayerID is " + selectedPlayerID);
    	playerList = getListView();
    	
    	//Populate the playerlist
		db.open();
		final Cursor data = db.getTeamPlayers(selectedTeamID);
		dataSource = new SimpleCursorAdapter(this, R.layout.row, data,fields, 
				new int[]{R.id.name});
		playerList.setHeaderDividersEnabled(true);
		
		setListAdapter(dataSource);
		db.close();

		//Select a player by clinking in listview
		playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt,
					long arg3) {
				selectedPlayerID = data.getInt(data.getColumnIndex(DBHelper.PLAYERID));
				System.out.println(selectedPlayerID);
				db.open();
				statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
				db.close();
			}
			
		}
		);
		
		//contextMenu for editing players 
		playerList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adView, View view,
					int pos, long id) {
				selectedPlayerID = data.getInt(data.getColumnIndex(DBHelper.PLAYERID));
				System.out.println(selectedPlayerID);
				playerList.showContextMenu();
				return true;
			}
		});
		registerForContextMenu(playerList);
		

        
            
        //Buttons for stats: AtWon, atNulled, atLost, block, seAce, service/passes 1-4.
        Button attackWonButton = (Button) findViewById(R.id.attackWonButton);
        attackWonButton.setOnClickListener(new OnClickListener() {
			
        	/**Add an attack won to the currently selected player*/
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 1);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " attacked and scored a point!", Toast.LENGTH_SHORT).show();
				}
				db.close();				
			}
		});
        
        Button attackNulledButton = (Button) findViewById(R.id.attackNulledButton);
        attackNulledButton.setOnClickListener(new OnClickListener() {
			
        	/**Add an attack nulled to the currently selected player*/
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 0);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " attacked and ball is still in play.", Toast.LENGTH_SHORT).show();
				}
				db.close();				
			}
		});
        
        Button attackLostButton = (Button) findViewById(R.id.attackLostButton);
        attackLostButton.setOnClickListener(new OnClickListener() {
			
        	/**Add an attack lost to the currently selected player*/
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, -1);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " attacked and lost.", Toast.LENGTH_SHORT).show();
				}
				db.close();				
			}
		});
        
        Button blockButton = (Button)findViewById(R.id.blockButton);
        blockButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 5);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " blocked for a point!", Toast.LENGTH_SHORT).show();
				}
				db.close();				
			}
		});
        
        Button serviceAceButton = (Button)findViewById(R.id.serviceAcebutton);
        serviceAceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 10);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " made a service ace!", Toast.LENGTH_SHORT).show();
				}
				db.close();				
			}
		});
        
        Button serveOneButton = (Button) findViewById(R.id.oneButton);
        serveOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					db.open();
					if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
						if(!db.hasStat(selectedPlayerID, selectedGameID)){
							db.insertStat(selectedPlayerID, selectedGameID);
						}
						db.statPlusOne(selectedPlayerID, selectedGameID, 11);
						statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " served a one.", Toast.LENGTH_SHORT).show();
				}
					db.close();	
				
			}
		});
        
        Button twoButton = (Button) findViewById(R.id.twoButton);
        twoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					db.open();
					if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
						if(!db.hasStat(selectedPlayerID, selectedGameID)){
							db.insertStat(selectedPlayerID, selectedGameID);
						}
						db.statPlusOne(selectedPlayerID, selectedGameID, 12);
						statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " served a two.", Toast.LENGTH_SHORT).show();
				}
					db.close();	
				
			}
		});
        
        Button threeButton = (Button) findViewById(R.id.threeButton);
        threeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					db.open();
					if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
						if(!db.hasStat(selectedPlayerID, selectedGameID)){
							db.insertStat(selectedPlayerID, selectedGameID);
						}
						db.statPlusOne(selectedPlayerID, selectedGameID, 13);
						statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " served a three.", Toast.LENGTH_SHORT).show();
				}
					db.close();	
				
			}
		});
        
        Button fourButton = (Button) findViewById(R.id.fourButton);
        fourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					db.open();
					if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
						if(!db.hasStat(selectedPlayerID, selectedGameID)){
							db.insertStat(selectedPlayerID, selectedGameID);
						}
						db.statPlusOne(selectedPlayerID, selectedGameID, 14);
						statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " served a four.", Toast.LENGTH_SHORT).show();
						}
					db.close();	
				
			}
		});
        
        Button passOneButton = (Button) findViewById(R.id.passGradeOneButton);
        passOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 21);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
					Toast.makeText(getApplicationContext(), 
							db.getName(selectedPlayerID) + " passed a one.", Toast.LENGTH_SHORT).show();
				}
				db.close();	
			}
		});
        
        Button passTwoButton = (Button) findViewById(R.id.passGradeTwoButton);
        passTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 22);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
				}
				Toast.makeText(getApplicationContext(), 
						db.getName(selectedPlayerID) + " passed a two.", Toast.LENGTH_SHORT).show();
				db.close();	
			}
		});
        
        Button passThreeButton = (Button) findViewById(R.id.passGradeThreeButton);
        passThreeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 23);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
				}
				Toast.makeText(getApplicationContext(), 
						db.getName(selectedPlayerID) + " passed a three.", Toast.LENGTH_SHORT).show();
				db.close();	
			}
		});
        
        Button passFourButton = (Button) findViewById(R.id.passGradeFourButton);
        passFourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.open();
				if(db.isTeamPlayer(selectedPlayerID, selectedTeamID)){
					if(!db.hasStat(selectedPlayerID, selectedGameID)){
						db.insertStat(selectedPlayerID, selectedGameID);
					}
					db.statPlusOne(selectedPlayerID, selectedGameID, 24);
					statsTextView.setText(db.playerStatString(selectedPlayerID, selectedGameID));
				}
				Toast.makeText(getApplicationContext(), 
						db.getName(selectedPlayerID) + " passed a four.", Toast.LENGTH_SHORT).show();
				db.close();	
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.printFile:
			//Her kan vi spørre om opponentIsSet og scoreIsSet 
			try {
				printFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.setScore:
			setScore();
			return true;
			
			
		case R.id.setOpponentName:
			setOpponentName();
			return true;
			
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	/**Method to set opponent name for the output textfile*/
	private void setOpponentName() {
		AlertDialog.Builder teamNameAlert = new AlertDialog.Builder(this);
		final EditText teamNameInput = new EditText(this);
		db.open();
		teamNameInput.setText(db.getGameOpponentName(selectedGameID));
		db.close();
		teamNameAlert.setTitle("Set opponentteam name");
		teamNameAlert.setMessage(" ");
		teamNameAlert.setView(teamNameInput);
		teamNameAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				db.open();
				db.setOpponentName(selectedGameID, teamNameInput.getText().toString());
				System.out.println("Opponent name set to " + db.getGameScore(selectedGameID));
				db.close();
			}
		});
        
		teamNameAlert.show();
	}

	
	/**Method to set score for the selected game in the GAMETABLE,
	 * and for use in the output textfile*/
	private void setScore() {
		AlertDialog.Builder scoreAlert = new AlertDialog.Builder(this);
		final EditText scoreInput = new EditText(this);
		db.open();
		scoreInput.setText(db.getGameScore(selectedGameID));
		db.close();
		scoreAlert.setTitle("Set game score");
		scoreAlert.setMessage(" ");
		scoreAlert.setView(scoreInput);
		scoreAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				db.open();
				db.setGameScore(selectedGameID, scoreInput.getText().toString());
				System.out.println("Game score set to " + db.getGameScore(selectedGameID));
				db.close();
			}
		});
        
		scoreAlert.show();
	}

	/**Method to print a textfile with stats for the whole team. To SD-Card
	 * */
	public void printFile() throws IOException{
		//Ha med noe hvis lagnavn eller score ikke er satt, så kjør de metodene
		db.open();
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    System.out.println("Yay, we can read AND write!");
			//Get path to root
		    File root = android.os.Environment.getExternalStorageDirectory();
			System.out.println(root);
			//Create a directory and file for given path 
			File dir = new File(root.getAbsolutePath() + "/VolleyStats");
			dir.mkdirs();
			String filename = db.getGameDate(selectedGameID) + " - " + db.getTeamName(selectedTeamID) + " - " +
			db.getGameOpponentName(selectedGameID) + ".txt";
			File file = new File(dir, filename);
			
			try {
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);
				
                pw.println(db.getGameDate(selectedGameID) + ": " + db.getTeamName(selectedTeamID) + " - " + db.getGameOpponentName(selectedGameID) + ": " + db.getGameScore(selectedGameID));
                pw.println();
                Cursor playerIDs = db.getTeamPlayerIDs(selectedTeamID);
                playerIDs.moveToFirst();
                while (playerIDs.isAfterLast()==false){
					pw.println(db.playerStatString(playerIDs.getInt(0), selectedGameID));
//					System.out.println("playerID: " + playerIDs.getInt(0));
					playerIDs.moveToNext();
					pw.println(" ");
				}	
                
                pw.flush();
                pw.close();
                f.close();
                Toast.makeText(getApplicationContext(), 
						filename + " was stored in "+ root.getAbsolutePath() + "/VolleyStats.", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "******* File not found. Did you" +
                                " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            } catch (IOException e) {
                e.printStackTrace();
            }	
		    
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    System.out.println("Can only read");
		} else {
		    System.out.println("Can neither read or write");
		}
		db.close();

	}

	/**Method to reload the activity, mainly called when updating database.*/
	public void reload(){
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Player edit");
		menu.add(0, v.getId(), 0, "Rename player");
		menu.add(0, v.getId(), 0, "Delete player");	
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getTitle()=="Delete player"){
			db.open();
			db.deletePlayer(selectedPlayerID);
			db.close();
			reload();
		}
		else if(item.getTitle()=="Rename player"){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			db.open();
			input.setText(db.getName(selectedPlayerID));
			db.close();
			alert.setTitle("Rename player");
			alert.setMessage("Type in player Name");
			
			alert.setView(input);
			
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newPlayerName = input.getText().toString();
					db.open();
					db.updatePlayer(selectedPlayerID, newPlayerName);
					db.close();
					reload();				
				}
			});
			alert.show();
		}
		else{
			return false;
		}
		return true;
	}


}