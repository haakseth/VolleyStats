package no.haakseth.volleystats;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * FIX:
 * - ListView of games need more info like date, score. Might
 * also need to change order (get most recent game first)
 * 
 * IDEAS:
 * - Get player's aggregate stats, number of games/
 * 		total points/points per game/etc on click
 * - 
 * 
 * */


/**
 * @author      John Wika Haakseth <johnwika@gmail.com>
 * @since       2012-01-30        
 * The application's second activity. Shows a list of the selected team's
 * players to the left and its games to the right as ListViews.
 * There are Buttons to create a new game or player. Creating a new game or selecting
 * one of the games in the gamelist will take the user to the next activity, GameActivity.
 * 
 * */
public class TeamActivity extends ListActivity {
	
	private ListView playerList;
	private ListView gameList;
	private TextView playerInfoTextView;
	final DBHelper db = new DBHelper(this);
	private CursorAdapter playerDataSource;
	private CursorAdapter gameDataSource;
	
	
	int selectedGameID = -1; //
	int selectedPlayerID = -1;
	
	
    private Button newGameButton;
    private Button newPlayerButton; //Might move to actionbar
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teamactivity);
		Intent teamIntent = getIntent();
		final int selectedTeamID = teamIntent.getIntExtra("selectedTeamID", -1);
		playerList = getListView();
		gameList = (ListView)findViewById(R.id.gamesListView);
		newGameButton = (Button)findViewById(R.id.newGameButton);
		newPlayerButton = (Button)findViewById(R.id.newPlayerButton);
		playerInfoTextView = (TextView)findViewById(R.id.playerInfoTextView);
		
		//Populate the playerlist
		db.open();
		final Cursor playerData = db.getTeamPlayers(selectedTeamID);
		playerDataSource = new SimpleCursorAdapter(this, R.layout.row, playerData,
				new String[]{DBHelper.PLAYERNAME}, 
				new int[]{R.id.name});
		setListAdapter(playerDataSource);
		db.close();
		
		//Populate the gamelist
		db.open();
		final Cursor gameData = db.getTeamGames(selectedTeamID);
		gameDataSource = new SimpleCursorAdapter(this, R.layout.row, gameData,
				new String[]{DBHelper.OPPONENTNAME,DBHelper.DATECREATED,DBHelper.RESULT}, 
				new int[]{R.id.name});
		gameList.setAdapter(gameDataSource);
		db.close();
		
		//Select a player by clinking in listview
		playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt,
					long arg3) {
				selectedPlayerID = playerData.getInt(playerData.getColumnIndex(DBHelper.PLAYERID));
				db.open();
				playerInfoTextView.setText(db.playerAggStatString(selectedPlayerID));
				db.close();
			}
			
		}
		);
		
		//Choose an existing game
		gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> myAdapter, View myView, int position,
					long arg3) {
				Cursor c = ((SimpleCursorAdapter)gameList.getAdapter()).getCursor();
				c.moveToPosition(position);
				selectedGameID = c.getInt(0);
				System.out.println("sent selectedGameID = " + selectedGameID);
				Intent gameIntent = new Intent(TeamActivity.this, GameActivity.class);
				gameIntent.putExtra("selectedGameID", selectedGameID);
				gameIntent.putExtra("selectedTeamID", selectedTeamID);
				gameIntent.putExtra("selectedPlayerID", selectedPlayerID);
				TeamActivity.this.startActivity(gameIntent);
			}
			
		}
		);
		//create a new game
		newGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(newGameButton.getContext());
				final EditText input = new EditText(newGameButton.getContext());
				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				alert.setTitle("New game");
				alert.setMessage("Type in opponent team Name");
				
				alert.setView(input);
				
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String opponentTeamName = input.getText().toString();
						db.open();
						selectedGameID = (int) db.insertGame(selectedTeamID, opponentTeamName);
						db.close();
						//Somehow send gameinfo to next activity, so that stats for players are updated for that game
						reload();
						Intent gameIntent = new Intent(TeamActivity.this, GameActivity.class);	
						gameIntent.putExtra("selectedGameID", selectedGameID);
						gameIntent.putExtra("selectedTeamID", selectedTeamID);
						gameIntent.putExtra("selectedPlayerID", selectedPlayerID);
						TeamActivity.this.startActivity(gameIntent);
					}
				});
				alert.show();
			}
				
			});
		
		newPlayerButton.setOnClickListener(new OnClickListener() {
			
        	/** When the newPlayerButton is clicked, an AlertDialog opens
        	 * for adding a player name, this player is then added to the team
        	 * and the playerList.*/
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(newPlayerButton.getContext());
				final EditText input = new EditText(newPlayerButton.getContext());
				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				alert.setTitle("Add a new player");
				alert.setMessage("Type in player Name");
				
				alert.setView(input);
				
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newPlayerName = input.getText().toString();
						db.open();
						db.insertPlayer(newPlayerName, selectedTeamID);
						db.close();
						reload();				
					}
				});
				alert.show();
				
				
			}
			
			
			
		});
	}
    
	public void reload(){
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

}
