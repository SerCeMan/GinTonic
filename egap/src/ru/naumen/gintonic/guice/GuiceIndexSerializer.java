package ru.naumen.gintonic.guice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.runtime.IPath;

import ru.naumen.gintonic.EgapPlugin;
import ru.naumen.gintonic.utils.DateUtils;

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
	 * @throws IOException
	 */
	public static GuiceIndex read(){

		ObjectInputStream deserializer = null;
		FileInputStream fileInputStream = null;

		try {
			File guiceIndexSerialized = getPathToGuiceIndexAsSerializedFile();
			if (!guiceIndexSerialized.exists()) {
				return null;
			}

			long now = System.currentTimeMillis();

			fileInputStream = new FileInputStream(guiceIndexSerialized);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					fileInputStream);
			deserializer = new ObjectInputStream(bufferedInputStream);
			GuiceIndex guiceIndex = (GuiceIndex) deserializer.readObject();

			long then = System.currentTimeMillis();
			long elapsed = then - now;
			EgapPlugin.logInfo("Succesfully read Guice index from file '"
					+ guiceIndexSerialized.getAbsolutePath() + "' ("
					+ DateUtils.formatMilliseconds(elapsed) + ").");
			EgapPlugin.logInfo(guiceIndex.getIndexInfoDetailed());
			return guiceIndex;
		}catch (Exception e) {
			EgapPlugin.logWarning("Error deserializing Guice index!");
		}
		finally {
			if (deserializer != null) {
				try {
					deserializer.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}

	/**
	 * Serializes the guice index to disc. Overwrites old index.
	 *
	 * @throws IOException
	 */
	public static void write() throws IOException {
		GuiceIndex guiceIndex = GuiceIndex.get();
		if (guiceIndex.getNrOfGuiceModules() == 0) {
			EgapPlugin.logInfo("Didn't write Guice index as it is empty.");
			return;
		}
		ObjectOutputStream serializer = null;
		FileOutputStream fileOutputStream = null;
		try {
			File guiceIndexSerialized = getPathToGuiceIndexAsSerializedFile();
			fileOutputStream = new FileOutputStream(guiceIndexSerialized);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					fileOutputStream);
			serializer = new ObjectOutputStream(bufferedOutputStream);
			serializer.writeObject(guiceIndex);
			EgapPlugin.logInfo("Succesfully written Guice index ("
					+ guiceIndex.getNrOfGuiceModules() + " modules) to file '"
					+ guiceIndexSerialized.getAbsolutePath() + "')");
		} finally {
			if (serializer != null) {
				serializer.close();
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
