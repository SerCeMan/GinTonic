package egap.guice;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.runtime.IPath;

import egap.EgapPlugin;

public class GuiceIndexSerializer {

	private static final String GUICE_INDEX_FILENAME = "GuiceIndex";

	public static void clear() {
		File pathToGuiceIndexAsSerializedFile = getPathToGuiceIndexAsSerializedFile();

		if (pathToGuiceIndexAsSerializedFile.exists()) {
			pathToGuiceIndexAsSerializedFile.delete();
		}
		EgapPlugin.logInfo("Succesfully cleared Guice index.");
	}

	/**
	 * Returns the index from disc or null if it does not exist or cannot be
	 * read.
	 */
	public static GuiceIndex read() throws IOException, ClassNotFoundException {
		
		ObjectInputStream deserializer = null;
		FileInputStream fileInputStream = null;
		try {
			File guiceIndexSerialized = getPathToGuiceIndexAsSerializedFile();

			if (!guiceIndexSerialized.exists()) {
				return null;
			}
			
			fileInputStream = new FileInputStream(guiceIndexSerialized);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			deserializer = new ObjectInputStream(bufferedInputStream);
			GuiceIndex guiceIndex = (GuiceIndex) deserializer.readObject();
			EgapPlugin.logInfo("Succesfully read Guice index from file '"
					+ guiceIndexSerialized.getAbsolutePath() + "'.");
			EgapPlugin.logInfo(guiceIndex.getIndexInfoDetailed());
			return guiceIndex;
		} finally {
			if (deserializer != null) {
				deserializer.close();
			}
		}
	}

	/**
	 * Serializes the guice index to disc. Overwrites old index.
	 * 
	 * @throws IOException
	 */
	public static void write() throws IOException {
		GuiceIndex guiceIndex = GuiceIndex.get();
		ObjectOutputStream serializer = null;
		FileOutputStream fileOutputStream = null;
		try {
			if (!EgapPlugin.isDebugMode()) {
				File guiceIndexSerialized = getPathToGuiceIndexAsSerializedFile();
				fileOutputStream = new FileOutputStream(guiceIndexSerialized);
				serializer = new ObjectOutputStream(fileOutputStream);
				serializer.writeObject(guiceIndex);
				EgapPlugin.logInfo("Succesfully written Guice index ("
						+ guiceIndex.getNrOfGuiceModules()
						+ " modules) to file '"
						+ guiceIndexSerialized.getAbsolutePath() + "')");
			}
		} finally {
			if (serializer != null) {
				serializer.close();
			}
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}

	/**
	 * Returns the path to the file where to persistently store the guice index.
	 * 
	 */
	public static File getPathToGuiceIndexAsSerializedFile() {
		try {
			EgapPlugin egapPlugin = EgapPlugin.getEgapPlugin();
			IPath stateLocation = egapPlugin.getStateLocation();
			File storageDirectory = stateLocation.toFile();
			File file = new File(storageDirectory, GUICE_INDEX_FILENAME);
			return file;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
