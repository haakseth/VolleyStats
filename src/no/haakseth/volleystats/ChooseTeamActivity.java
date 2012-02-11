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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.provider.BaseColumns;

/**
 * @author      John Wika Haakseth <johnwika@gmail.com>
 * @since       2012-01-30     
 * The application's first activity. Shows a list of the existing
 * teams in the database, and a button to create a new team. Choosing a team
 * or creating a new team will take the user to the next Activity, TeamActivity.
 * 
 * */
public class ChooseTeamActivity extends ListActivity {

	private ListView teamList;
	final DBHelper db = new DBHelper(this);
	private CursorAdapter datasource;
	
    int selectedTeamID = -1;
    private Button newTeamButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooseteam);
		
		teamList = getListView();
		teamList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//kanskje?
		newTeamButton = (Button)findViewById(R.id.newTeamButton);
		
		
		
		
		//populate the teamlist
		db.open();
		final Cursor data = db.getAllTeams();
		datasource = new SimpleCursorAdapter(this, R.layout.row, data,
				new String[]{DBHelper.TEAMNAME}, 
				new int[]{R.id.name});
		setListAdapter(datasource);
		db.close();
		
		teamList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Cursor c = ((SimpleCursorAdapter)teamList.getAdapter()).getCursor();
				c.moveToPosition(position);
				selectedTeamID = c.getInt(0);
//				System.out.println("selectedTeamID = " + selectedTeamID);
				db.open();
				System.out.println("Team has " + db.getTeamSize(selectedTeamID) + " players.");
				db.close();
				//Somehow send which team we're working on to next activity
				Intent teamIntent = new Intent(ChooseTeamActivity.this, TeamActivity.class);
				teamIntent.putExtra("selectedTeamID", selectedTeamID);
				ChooseTeamActivity.this.startActivity(teamIntent);				
			}
		});
		
		newTeamButton.setOnClickListener(new OnClickListener() {
			
        	/** When the newButton is clicked, an AlertDialog opens
        	 * for adding a team name, this team is then added to the database.*/
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(newTeamButton.getContext());
				final EditText input = new EditText(newTeamButton.getContext());
				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				alert.setTitle("Add a team");
				alert.setMessage("Type in team Name");
				
				alert.setView(input);
				
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newTeamName = input.getText().toString();
						db.open();
						db.insertTeam(newTeamName);
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
//		overridePendingTransition(0, 0);
		startActivity(intent);
	}
}
