package net.kingingo.server.music;

import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.hc.core5.http.ParseException;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.mysql.MySQL;
import net.kingingo.server.utils.TimeSpan;

public class spotifyHandler implements Runnable {
	private static final String MYSQL_TABLE = "spotify_credentials";

	private static HashMap<String, spotifyHandler> handlers = new HashMap<>();
	private static final URI redirectUri = SpotifyHttpManager.makeUri("http://54.38.22.48/wg/");

	public static spotifyHandler get(String name) {
		return handlers.get(name);
	}

	private Thread thread;
	private boolean active = true;
	private State state;
	private URI code_uri;
	@Setter
	private String code;
	private long expired_in = 0;

	private String clientId;
	private String clientSecret;
	private String name;

	private String accessToken;
	private String refreshToken;

	private SpotifyApi spotifyApi;

	public spotifyHandler(String name) {
		this(name, "", "");
	}

	public spotifyHandler(String name, String clientId, String clientSecret) {
		this.name = name;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.init();
		spotifyHandler.handlers.put(this.name, this);

		MySQL.Update("CREATE TABLE IF NOT EXISTS `spotify_credentials` (`name` varchar(50) DEFAULT NULL,"
				+ "`clientId` varchar(50) DEFAULT NULL," + "`clientSecret` varchar(50) DEFAULT NULL,"
				+ "  `accessToken` varchar(50) DEFAULT NULL," + "  `refreshToken` varchar(50) DEFAULT NULL,"
				+ "  `expired_in` bigint(20) DEFAULT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
	}

	public void insertCredentials() {
		MySQL.Update("INSERT INTO " + MYSQL_TABLE + " (name, clientId, clientSecret) VALUES ('" + this.name + "', '"
				+ this.clientId + "', '" + this.clientSecret + "')");
	}

	public boolean loadCredentials() {
		try {
			Statement stmt = MySQL.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + MYSQL_TABLE + " WHERE `name`='" + this.name + "';");

			boolean found = false;
			while (rs.next()) {
				found = true;
				this.clientId = rs.getString("clientId");
				this.clientSecret = rs.getString("clientSecret");
				this.expired_in = rs.getLong("expired_in");

				this.accessToken = rs.getString("accessToken");
				this.refreshToken = rs.getString("refreshToken");
			}
			stmt.close();

			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void updateCredentials() {
		MySQL.Update("UPDATE " + MYSQL_TABLE + " SET " + "`accessToken`='" + this.spotifyApi.getAccessToken() + "', "
				+ "`refreshToken`='" + this.spotifyApi.getRefreshToken() + "', " + "`expired_in`='" + this.expired_in
				+ "' " + "WHERE `name`='" + this.name + "';");
	}

	@Override
	public void run() {
		while (this.active) {
			switch (this.state) {
			case LOGGED_IN:
				if ((this.expired_in - System.currentTimeMillis()) < TimeSpan.MINUTE) {
					this.state = State.REFRESH;
					Main.printf("AccessToken is expiring in one minute so REFRESH!!!");
				} else {
					try {
						GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = this.spotifyApi
								.getUsersCurrentlyPlayingTrack().build();
						CurrentlyPlaying currentlyPlaying = getUsersCurrentlyPlayingTrackRequest.execute();
						Main.printf("Progess: " + currentlyPlaying.getProgress_ms() + "ms");
						Main.printf("Is playing: " + currentlyPlaying.getIs_playing());
						Main.printf("Item: " + currentlyPlaying.getItem().getName());
						Track track = ((Track) currentlyPlaying.getItem());

						Main.printf("Track ExternalUrls: " +  track.getExternalUrls());
						Main.printf("Track Uri: " +  track.getUri());
						Main.printf("Track PreviewUrl: " +  track.getPreviewUrl());
						Main.printf("Track Artists: " +  track.getArtists());
						Main.printf("Track Duration: " +  track.getDurationMs());
						Main.printf("Id: " + track.getId());
						Main.printf("Album-Id: " + track.getAlbum().getId());
						Image[] images = track.getAlbum().getImages();
						Main.printf("Images: ");
						for (Image img : images) {
							Main.printf("	" + img);
						}
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SpotifyWebApiException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						Thread.sleep(1000 * 20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case REFRESH:
				try {
					final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = this.spotifyApi
							.authorizationCodeRefresh().build();
					AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest
							.execute();

					// Set access and refresh token for further "spotifyApi" object usage
					spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
					this.expired_in = authorizationCodeCredentials.getExpiresIn() * 1000 + System.currentTimeMillis();
					Main.printf("AccessToken: " + authorizationCodeCredentials.getAccessToken());
					Main.printf("Expires in: " + this.expired_in);
					updateCredentials();
					this.state = State.LOGGED_IN;
					continue;
				} catch (ParseException | SpotifyWebApiException | IOException e1) {
					e1.printStackTrace();
				}

				try {
					Thread.sleep(1000 * 15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case GET_URI:
				if (this.code != null) {
					this.state = State.GET_ACCESS_TOKEN;
				} else {
					if (this.code_uri == null) {
						final AuthorizationCodeUriRequest authorizationCodeUriRequest = this.spotifyApi
								.authorizationCodeUri().scope("user-read-currently-playing").build();
						this.code_uri = authorizationCodeUriRequest.execute();
					}
					Main.printf("############# OEFFNEN & AKZEPTIEREN #############");
					Main.printf("URI: " + this.code_uri.toString());
					Main.printf("### CODE MIT 'spotify [name] [code]' EINGEBEN ###");
					try {
						Thread.sleep(1000 * 15);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case GET_ACCESS_TOKEN:
				try {
					final AuthorizationCodeRequest authorizationCodeRequest = this.spotifyApi.authorizationCode(code)
							.build();
					final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

					// Set access and refresh token for further "spotifyApi" object usage
					this.spotifyApi.setAccessToken(credentials.getAccessToken());
					this.spotifyApi.setRefreshToken(credentials.getRefreshToken());
					this.expired_in = credentials.getExpiresIn() * 1000 + System.currentTimeMillis();

					Main.printf("AccessToken   " + credentials.getAccessToken());
					Main.printf("RefreshToken   " + credentials.getRefreshToken());
					Main.printf("Expires in: " + this.expired_in);
					updateCredentials();

					this.state = State.LOGGED_IN;
					continue;
				} catch (IOException | SpotifyWebApiException | ParseException e) {
					Main.error("Error: " + e.getMessage());
				}

				try {
					Thread.sleep(1000 * 15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private void init() {
		boolean found = this.loadCredentials();

		if (!found) {
			if (!this.clientId.isEmpty() && !this.clientSecret.isEmpty()) {
				insertCredentials();
				this.state = State.GET_URI;
			} else {
				throw new NullPointerException("clientId or clientSecret is null?! " + this.name);
			}
		} else {
			this.state = State.LOGGED_IN;
		}

		Main.printf("spotify Handler " + this.state.name() + " initiliaze... " + this.name + " " + this.clientId + ":"
				+ this.clientSecret);
		this.spotifyApi = new SpotifyApi.Builder().setClientId(this.clientId).setClientSecret(this.clientSecret)
				.setRedirectUri(redirectUri).build();

		if (this.accessToken != null)
			this.spotifyApi.setAccessToken(this.accessToken);

		if (this.refreshToken != null)
			this.spotifyApi.setRefreshToken(this.refreshToken);

		this.thread = new Thread(this);
		this.thread.setName("spotify-handler-"+this.name);
		this.thread.start();
	}

	private enum State {
		GET_URI, GET_ACCESS_TOKEN, LOGGED_IN, REFRESH;
	}
}
