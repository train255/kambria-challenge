package com.example;

import java.io.IOException;

import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

/**
 * 
 * This class tests the JLibrosa functionality for extracting MFCC and STFT
 * Audio features for given Wav file.
 * 
 * @author abhi-rawat1
 *
 */
public class Main {

	public static void main(String[] args) throws IOException, WavFileException, FileFormatNotSupportedException {
		String audioDir = "audioFiles";
		String featuresDir = "mfcc_features";
		if (args.length > 0) {
			audioDir = args[0];
		}
		if (args.length > 1) {
			featuresDir = args[1];
		}
		File folder = new File(audioDir);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fileNameWithOutExt = file.getName().replaceFirst("[.][^.]+$", "");
				String audioFilePath = audioDir + "/" + file.getName();
				int defaultSampleRate = -1; // -1 value implies the method to use default sample rate
				int defaultAudioDuration = -1; // -1 value implies the method to process complete audio duration

				JLibrosa jLibrosa = new JLibrosa();

				/*
				 * To read the magnitude values of audio files - equivalent to
				 * librosa.load('../audioFiles/1995-1826-0003.wav', sr=None) function
				 */

				float audioFeatureValues[] = jLibrosa.loadAndRead(audioFilePath, defaultSampleRate,
						defaultAudioDuration);

				/*
				 * To read the MFCC values of an audio file
				 * equivalent to librosa.feature.mfcc(x, sr, n_mfcc=40) in python
				 */
				int n_mfcc = 96;
				int n_fft = 4096;
				int hop_length = 512;
				int n_mels = 512;
				float[][] mfccValues = jLibrosa.generateMFCCFeatures(
						audioFeatureValues, 8000, n_mfcc, n_fft, n_mels, hop_length);

				System.out.println(".......");
				System.out.println(
						"Size of MFCC Feature Values: (" + mfccValues.length + " , " + mfccValues[0].length + " )");

				String featurePath = featuresDir + "/" + fileNameWithOutExt + ".txt";
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < mfccValues.length; i++)// for each row
				{
					for (int j = 0; j < mfccValues[i].length; j++)// for each column
					{
						builder.append(mfccValues[i][j] + "");// append to the output string
						if (j < mfccValues.length - 1)// if this is not the last row element
							builder.append(",");// then add comma (if you don't like commas you can use spaces)
					}
					builder.append("n");// append new line at the end of the row
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(featurePath));
				writer.write(builder.toString());// save the string representation of the mfccValues
				writer.close();
			}
		}

	}

}
