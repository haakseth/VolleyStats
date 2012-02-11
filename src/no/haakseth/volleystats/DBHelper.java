package no.haakseth.volleystats;

/**FIXES FOR COMMON ERRORS
 * android.database.CursorIndexOutOfBoundsException: Index -1 requested, with a size of 1.
 * --- call Cursor.moveToFirst();
 * */

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

/**
 * @author      John Wika Haakseth <johnwika@gmail.com>
 * @since       2012-01-30      
 * 
 * Helper class with variables and methods for interacting with the
 * database.
 * 
 * The database is named volleyball.db and has the 
 * tables PLAYERS, TEAMS, STATS, GAMES.
 * */
public class DBHelper {

	//Database schema
	public static final String DATABASE_NAME = "volleyball.db";
	public static final String TEAMS_TABLE = "teams";
	public static final String PLAYER_TABLE = "players";
	public static final String STATS_TABLE = "stats";
	public static final String GAMES_TABLE = "games";
	public static final int DATABASE_VERSION = 1;
	
	//Teams table
	public static final String TEAMID = "_teamID"; //integer primary key autoincremet
	public static final String TEAMNAME = "teamname"; //text, ex: "Harambee H1"
	
	//Games table
	public static final String GAMEID = "_gameID"; // integer primary key autoincrement
	public static final String GTEAM = "teamid"; //foreign key for team
	public static final String OPPONENTNAME = "opponent"; //text, ex: "DeVoKo H1" 
	public static final String RESULT = "result"; //text, ex "3-1 (25-21, 23-25, 25-19, 26-24)"
	public static final String GAMEWON = "gamewon"; //integer: 1 for won, 0 for lost (-1 for incomplete)
	public static final String DATECREATED = "dateCreated"; //integer: 1 for won, 0 for lost (-1 for incomplete)
	public static final String DATEEDIT = "dateLastEdit"; //integer: 1 for won, 0 for lost (-1 for incomplete)
	
	//Players table
	public static final String PLAYERID = "_id";
	public static final String PLAYERNAME = "playerName";
	public static final String PTEAMID = "teamid"; // foreign key for team id
	
	//Stats table
	public static final String STATSID = "_statsID"; //integer primary key autoincrement
	public static final String SPLAYER = "playerid"; //foreign key playerid
	public static final String SGAME = "gameid"; //foreign key gameid
	public static final String ATWON = "atWon"; //integer
	public static final String ATNULLED = "atNulled"; //int
	public static final String ATLOST = "atLost"; //int
	public static final String BLOCKS = "blocks"; //int
	public static final String SERVEACES = "serveAces"; //int
	public static final String SEONES = "seOnes"; //int
	public static final String SETWOS = "seTwos"; //int
	public static final String SETHREES = "seThrees"; //int
	public static final String SEFOURS = "seFours"; //int
	public static final String PAONES = "paOnes"; //int
	public static final String PATWOS = "paTwos"; //int
	public static final String PATHREES = "paThrees"; //int
	public static final String PAFOURS = "paFours"; //int
	
	public static final String TAG = "DBAdapter";
	
	
	//queries for creating database tables
	private static final String TEAMTABLE_CREATE = 
			"create table if not exists " + TEAMS_TABLE +
				"("+ TEAMID +" integer primary key autoincrement, " +
				TEAMNAME + " text);";
	
	private static final String GAMETABLE_CREATE = 
			"create table if not exists " + GAMES_TABLE +
				"(" + GAMEID + " integer primary key autoincrement, " + 
				GTEAM + " integer not null, " + 
				OPPONENTNAME +  " text, "+
				RESULT + " text, " + 
				DATECREATED + " text, " +
				DATEEDIT + " text, " +
				"foreign key ("+ GTEAM +") references " + 
					TEAMS_TABLE + "(" + TEAMID  + "));";
	
	private static final String PLAYERTABLE_CREATE = 			
			"create table if not exists "+PLAYER_TABLE+" (" +
				PLAYERID +" integer primary key autoincrement, " +
				PLAYERNAME+" text not null, "+
				PTEAMID+" integer not null," +
				"foreign key(" +PTEAMID+") references "+ TEAMS_TABLE+"("+TEAMID+")"+
				");";
	
	private static final String STATSTABLE_CREATE = 
			"create table if not exists "+STATS_TABLE+" (" +
					STATSID +" integer primary key autoincrement, " +
					SPLAYER +" integer not null, " + 
					SGAME + " integer not null, " + 
					ATWON + " integer, " +
					ATNULLED + " integer, " + 
					ATLOST + " integer, " + 
					BLOCKS + " integer, " + 
					SERVEACES + " integer, " + 
					SEONES + " integer, " + 
					SETWOS + " integer, " + 
					SETHREES + " integer, " + 
					SEFOURS + " integer, " + 
					PAONES + " integer, " + 
					PATWOS + " integer, " + 
					PATHREES + " integer, " + 
					PAFOURS + " integer, " + 
					"foreign key(" +SPLAYER+") references "+ PLAYER_TABLE+"("+PLAYERID+")"+
					"foreign key(" +SGAME+") references "+ GAMES_TABLE+"("+GAMEID+")"+
					");";
	
	private final Context context;
	
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	/**Constructor*/
	public DBHelper(Context ctx){
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	/**Extend class to make instance of SQLiteOpenHelper*/
	private static class DatabaseHelper extends SQLiteOpenHelper{
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TEAMTABLE_CREATE);
			db.execSQL(GAMETABLE_CREATE);
			db.execSQL(PLAYERTABLE_CREATE);
			db.execSQL(STATSTABLE_CREATE);
			System.out.println("Database created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrade db from version " + oldVersion + " to " + 
					newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS players");
			onCreate(db);
		}
	}
	
	/**Open the database, call before making a call to DBHelper
	 * instance from another class, make sure to call close()
	 * after.*/
	public DBHelper open() throws SQLException{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	/**Close the database*/
	public void close(){
		DBHelper.close();
	}
	
	/**
	 * METHODS FOR INTERACTING WITH PLAYER TABLE
	 * */
	
	/**insert a new player into the database*/
	public long insertPlayer(String name, int teamid){
		ContentValues initialValues = new ContentValues();
		initialValues.put(PLAYERNAME, name);
		initialValues.put(PTEAMID, teamid);
		System.out.println("Inserting " + name + " to database");
		return db.insert(PLAYER_TABLE, null, initialValues);
	}
	
	/**delete a particular player*/
	public boolean deletePlayer(long rowID){
		
		return db.delete(PLAYER_TABLE, PLAYERID + " = " + rowID, null)>0;
	}
	
	/**retrieve a particular player:
	 * select KEY_ROWID,KEY_NAME,KEY_TEAM from PLAYERS where KEY_ROWID=rowId*/
	public Cursor getPlayer(long rowId) throws SQLException{
		Cursor mCursor = 
				db.query(true, PLAYER_TABLE, new String[]{
						PLAYERID,
						PLAYERNAME,
						PTEAMID,
						}, 
						PLAYERID + " = " + rowId,
				null, null, null, null, null);
		if(mCursor!= null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**Retrieve player name*/
	public String getName(int playerID){
		Cursor c = db.rawQuery("select " + PLAYERNAME + " from " + PLAYER_TABLE + 
				" where " + playerID + " = " + PLAYERID, null);
		if(c.moveToFirst()){
			return c.getString(0);			
		}
		else return null;
	}
	
	/**Upgrade a player*/
	public boolean updatePlayer(long playerID, String name){
		ContentValues args = new ContentValues();
		args.put(PLAYERNAME, name);
//		args.put(KEY_TEAM, team);
		return db.update(PLAYER_TABLE, args, PLAYERID + "=" + playerID, null)>0;
	}
	
	/**Used for populating ListViews
	 * @return Cursor with all players*/
	public Cursor getAllPlayers(){
		return db.query(PLAYER_TABLE, new String[]{
				PLAYERID,
				PLAYERNAME,
				PTEAMID,
		}, 
		null, null, null, null, null);
	}
	
	/**Used for populating ListView
	 * @return Cursor with all players for given team*/
	public Cursor getTeamPlayers(int teamID){
		return db.query(PLAYER_TABLE, new String[]{
				PLAYERID,
				PLAYERNAME,
				PTEAMID,
		}, 
		teamID + " = " + PTEAMID, null, null, null, null);
	}
	
	/**Check if given player is on given team, mainly to make
	 * sure selectedPlayerID is set, so Stat is not added to
	 * wrong player.*/
	public boolean isTeamPlayer(int playerID, int teamID){
		Cursor c = db.query(PLAYER_TABLE, new String[]{
				PLAYERID,
				PLAYERNAME,
				PTEAMID,
		}, 
		teamID + " = " + PTEAMID+
		" and " + playerID + " = " + PLAYERID, null, null, null, null);
		return c.moveToFirst();
	}
	
	/**Return Cursor with IDs of players belonging to given team*/
	public Cursor getTeamPlayerIDs(int teamID){
		Cursor c = db.query(PLAYER_TABLE, 
				new String[]{PLAYERID}, 
				teamID + " = " + PTEAMID
				, null, null, null, null);
		return c;
	}
	
	/**
	 * METHODS FOR INTERACTING WITH TEAM TABLE
	 * */
	
	/**insert a new team into the database*/
	public long insertTeam(String name){
		ContentValues initialValues = new ContentValues();
		initialValues.put(TEAMNAME, name);
		System.out.println("Inserting " + name + " to database");
		return db.insert(TEAMS_TABLE, null, initialValues);
	}
	
	/**Retrieve all teams, for populating ListView
	 * @return Cursor of all teams
	 * */
	public Cursor getAllTeams(){
		return db.query(TEAMS_TABLE, new String[]{
				TEAMID + " as _id",TEAMNAME
		}, null, null, null, null, null);
	}
	
	/**Get team name for given team id.
	 * @param teamID int representing the team's unique
	 * id in the database, usually passed with selectedTeamID*/
	public String getTeamName(int teamID){
		Cursor c = db.query(TEAMS_TABLE, 
				new String[]{TEAMNAME}, TEAMID + " = " + teamID, null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
	}
	
	/**Return the number of players belonging to given team.*/
	public int getTeamSize(int teamID){
		Cursor c = db.query(PLAYER_TABLE, new String[]{"count(" + PLAYERID +")"}, 
					teamID +" = "+ PTEAMID, 
					null, null, null, null);
		c.moveToFirst();
		return c.getInt(0);
	}
	
	/**
	 * METHODS FOR INTERACTING WITH GAME TABLE
	 * */
	
	
	/**insert a new game into the database
	 * 
	 * @param teamID Primary key of team player belongs to, collected from selectedTeamID
	 * @param opponent Name of opposing team.*/
	public long insertGame(int teamID, String opponent){
		ContentValues initialValues = new ContentValues();
		initialValues.put(GTEAM, teamID);
		initialValues.put(OPPONENTNAME, opponent);
		initialValues.put(RESULT, "0-0 (0-0, 0-0, 0-0)");
		initialValues.put(DATECREATED, getDateTime());
		System.out.println("Inserting new game to database");
		return db.insert(GAMES_TABLE, null, initialValues);
	}
	
	/**Retrieve all games, for populating ListView*/
	public Cursor getAllGames(){
		return db.query(GAMES_TABLE, new String[]{
				GAMEID + " as _id",OPPONENTNAME,DATECREATED,RESULT
		}, null, null, null, null, null);
	}
	
	/**Retrieve all games for given team*/
	public Cursor getTeamGames(int teamID){
		return db.query(GAMES_TABLE, new String[]{
				GAMEID + " as _id",GTEAM,OPPONENTNAME,DATECREATED,RESULT
		}, teamID +" = "+ GTEAM , null, null, null, null);
	}
	
	/**Set game score*/
	public boolean setGameScore(int gameID, String score){
		ContentValues args = new ContentValues();
		args.put(RESULT, score);
		return db.update(GAMES_TABLE, args, GAMEID + "=" + gameID, null)>0;
	}
	
	/**Get game score as string*/
	public String getGameScore(int gameID){
		Cursor c = db.query(GAMES_TABLE, new String[]{RESULT}, GAMEID + " = " +gameID, null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
	}
	
	/**Get game opponent game as string*/
	public String getGameOpponentName(int gameID){
		Cursor c = db.query(GAMES_TABLE, new String[]{OPPONENTNAME}, GAMEID + " = " +gameID, null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
	}
	
	/**Set opponent team name*/
	public boolean setOpponentName(int gameID, String name){
		ContentValues args = new ContentValues();
		args.put(OPPONENTNAME, name);
		return db.update(GAMES_TABLE, args, GAMEID + "=" + gameID, null)>0;
	}
	
	/**Return date and time of game*/
	public String getGameDate(int gameID){
		Cursor c = db.query(GAMES_TABLE, new String[]{DATECREATED}, 
				gameID + " = " + GAMEID, null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
	}
	
	/**
	 * METHODS FOR INTERACTING WITH STAT TABLE
	 * */
	
	/**insert a new team into the database*/
	public long insertStat(int playerID, int gameID){
		ContentValues initialValues = new ContentValues();
		initialValues.put(SPLAYER, playerID);
		initialValues.put(SGAME, gameID);
		initialValues.put(ATWON, 0);
		initialValues.put(ATNULLED, 0);
		initialValues.put(ATLOST, 0);
		initialValues.put(BLOCKS, 0);
		initialValues.put(SERVEACES, 0);
		initialValues.put(SEONES, 0);
		initialValues.put(SETWOS, 0);
		initialValues.put(SETHREES, 0);
		initialValues.put(SEFOURS, 0);
		initialValues.put(PAONES, 0);
		initialValues.put(PATWOS, 0);
		initialValues.put(PATHREES, 0);
		initialValues.put(PAFOURS, 0);
		System.out.println("Inserting new stat");
		return db.insert(STATS_TABLE, null, initialValues);
	}
	
	/**retrieve all stats for given game*/
	public Cursor getGameStats(int gameID){
		return db.query(STATS_TABLE, new String[]{
				"*"}, 
				gameID + " = " + SGAME, null, null, null, null);
	}
	
	/**Retrieve all stats for given player*/
	public Cursor getPlayerStats(int playerID){
		return db.query(STATS_TABLE, new String[]{
				GAMEID + " as _id",GTEAM,OPPONENTNAME,DATECREATED,RESULT
		}, playerID +" = "+ SPLAYER , null, null, null, null);
	}
	
	
	
	/**retrieve a particular stat*/
	public Cursor getStat(int gameID, int playerID) throws SQLException{
		Cursor mCursor = 
				db.query(true, STATS_TABLE, new String[]{
						"*"}, 
						gameID + " = " + SGAME + " AND " + playerID + " = " + SPLAYER,
				null, null, null, null, null);
		if(mCursor!= null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**Check if this player has a stat for this game, 
	 * called when statbutton is pressed during game to 
	 * check whether a new stat record need to be 
	 * inserted.*/
	public boolean hasStat(int playerID, int gameID){
		Cursor c = db.rawQuery("select * from " + STATS_TABLE + 
				" where " + gameID + " = " +SGAME + " and " + playerID + " = " + SPLAYER, null);
//		System.out.println(c.getCount());
		return c.moveToFirst();
	}
	
	
	//
	//EDIT STATS
	/**Add a stat by one, called when button is pushed during game
	 * @param plusnullminus 1 is an atWon, 0 is a atNulled and -1 is a atLost. 
	 * 5 - block, 10 - seAces
	 * 11 - seOnes, 12 - seTwos, 13 - seThrees, 14 - seFours
	 * 21 - paOnes, 22 - paTwos, 23 - paThrees, 24 - paFours
	 * @param playerID the owner of the stat to be updated.*/
	public boolean statPlusOne(int playerID, int gameID, int stat){
		Cursor cursor = getStat(gameID, playerID);
		int setInt;
		ContentValues args = new ContentValues();
		switch (stat) {
		case 1:
			//add an attack scored
			setInt = cursor.getInt(3)+1; //finn statistikken her
			args.put(ATWON, setInt);
			System.out.println("setting atWon to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 0:
			//add an attack nulled
			setInt = cursor.getInt(4)+1; 
			args.put(ATNULLED, setInt);
			System.out.println("setting atNulled to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;	
		case -1:
			//add an attack lost
			setInt = cursor.getInt(5)+1; 
			args.put(ATLOST, setInt);
			System.out.println("setting atLost to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 5:
			//add a block
			setInt = cursor.getInt(6)+1; 
			args.put(BLOCKS, setInt);
			System.out.println("setting blocks to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 10:
			//add a service ace
			setInt = cursor.getInt(7)+1; 
			args.put(SERVEACES, setInt);
			System.out.println("setting seAces to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 11:
			//add a seOne
			setInt = cursor.getInt(8)+1; 
			args.put(SEONES, setInt);
			System.out.println("setting seONES to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 12:
			//add a seTwo
			setInt = cursor.getInt(9)+1; 
			args.put(SETWOS, setInt);
			System.out.println("setting seTwo to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 13:
			//add a seThree
			setInt = cursor.getInt(10)+1;
			args.put(SETHREES, setInt);
			System.out.println("setting seThree to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 14:
			//add a seFour
			setInt = cursor.getInt(11)+1;
			args.put(SEFOURS, setInt);
			System.out.println("setting seFour to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 21:
			//add a paOne
			setInt = cursor.getInt(12)+1;
			args.put(PAONES, setInt);
			System.out.println("setting paOne to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 22:
			//add a paTwo
			setInt = cursor.getInt(13)+1;
			args.put(PATWOS, setInt);
			System.out.println("setting paTwo to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 23:
			//add a paThree
			setInt = cursor.getInt(14)+1;
			args.put(PATHREES, setInt);
			System.out.println("setting paThree to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		case 24:
			//add a paFour
			setInt = cursor.getInt(15)+1;
			args.put(PAFOURS, setInt);
			System.out.println("setting paFours to " + setInt);
			return db.update(STATS_TABLE, args, 
					SPLAYER + "=" + playerID + " and " + SGAME + "=" + gameID, null)>0;
		default:
			return false;
		}
		
	}
	

	
	/**Return string of player's stats, for updating TextView
	 * during GameAvtivity, to give user feedback on update.*/
	public String playerStatString(int playerID, int gameID){
		if(hasStat(playerID, gameID)){
			Cursor c = db.rawQuery("select * from " + STATS_TABLE + 
					" where " + gameID + " = " +SGAME + " and " + playerID + " = " + SPLAYER, null);
			c.moveToFirst();
			int totalServes = c.getInt(8) + c.getInt(9) + c.getInt(10) + c.getInt(11);
			int totalPasses = c.getInt(12) + c.getInt(13) + c.getInt(14) + c.getInt(15);
			DecimalFormat df = new DecimalFormat("#.#");
			double serviceAv = getAverage(c.getInt(8), c.getInt(9), c.getInt(10), c.getInt(11)); df.format(serviceAv);
			double passingAv = getAverage(c.getInt(12), c.getInt(13), c.getInt(14), c.getInt(15));df.format(passingAv);
			return getName(playerID) + 
					"\nPoints scored " + getPointsScored(c.getInt(3), c.getInt(6), c.getInt(7)) + 
					" points (" + c.getInt(3) + " Att, " + c.getInt(6) + " Bl, " + c.getInt(7) + "SA)." +
					"\nAttacking effect: " + getEffect(c.getInt(3), c.getInt(5), c.getInt(4)) + "% on " + 
						getPointsScored(c.getInt(3), c.getInt(5), c.getInt(4)) + " attempts ("+ 
						c.getInt(3) + " Won, " + c.getInt(4) + " nulled, "+ c.getInt(5) + " lost)" +"." +
					"\nService average: " + serviceAv + " on " + totalServes + " attempts." + 
					"\nPassing average: " + passingAv + " on " + totalPasses + " attempts.";
		}
		else {
			return "There are no recorded stats for " + getName(playerID) + " for this game.";
		}
	}
	
	/**Return a String of player's aggregate stats, for use in
	 * TeamActivity
	 * @param playerID The selected player, who's aggregated stats we will display.
	 * */
	public String playerAggStatString(int playerID){
		Cursor c = db.rawQuery("select count("+STATSID + ")," + 
				"sum("+ATWON + ")," + 
				"sum("+BLOCKS + ")," + 
				"sum("+SERVEACES + ")" + 
				" from " + STATS_TABLE + 
				" where " + playerID + " = " + SPLAYER, null);
		c.moveToFirst();
		return getName(playerID) + " has played " + c.getInt(0) + " games." +
				"Total ponts scored: " + getPointsScored(c.getInt(1), c.getInt(2), c.getInt(3));
	}
	
	/**Return current date and time as String, for games*/
	public String getDateTime(){
		//method to create a String of the current date, used in the filenaming of the printed textfile
		  final String DATE_FORMAT_NOW = "yyyy-MM-dd - HHmm";
		  Calendar cal = Calendar.getInstance();
		  SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		  return sdf.format(cal.getTime());
	}
	
	/**Methods for calculating stats*/
	
	public int getEffect(double plus, double minus, double nulled){
		double effect;
		int effectInt;
		if(plus+minus+nulled!=0){
			effect = ((plus - minus)/(plus + minus + nulled))*100;
			effectInt = (int)effect;
			return effectInt;
		}
			
		else return 0;
	}
	
	public double getAverage(double ones, double twos, double threes, double fours){
		double ave = 0;
		double total = ones+twos+threes+fours;
		if(total!=0)
			ave = (1*ones+2*twos+3*threes+4*fours)/total;
		
		return ave;
	}
	
	public int getTotalAttempts(int ones, int twos, int threes, int fours){
		int total = ones+twos+threes+fours;
		return total;
	}

	public int getPointsScored(int attacks, int blocks, int serviceAces){
		return attacks+blocks+serviceAces;
	}
}
