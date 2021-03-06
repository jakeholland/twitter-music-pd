package edu.uiuc.sigmusic.twittersounds;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author SIGMusic Spring 2012
 * 
 *         MelodyGenerator class
 * 
 *         Handles almost all aspects of generating music
 */

public class MelodyGeneratorJoe {

	/**
	 * Default DEBUG values... Key is generated (for now, C) Chord Progression
	 * is generated (for now, I IV I V) Synth and Bass will just play scales
	 * Drums will be generated
	 */

	public boolean DEBUG = false; // false To generate a random melody, true to
									// generate straight scales for testing
	public boolean fileRead = false; // Flag to see if file reading was
										// successful
	MelodyGeneratorJoe prev;

	/**
	 * Mood Attributes
	 */

	public int happiness = 0; // Happiness parameter, from 0 to 100, 0 =
								// depressed, 100 = elated
	public int excitement = 0; // Excitement parameter, from 0 to 100, 0 =
								// bored, 100 = excited
	public int confusion = 0; // Confusion parameter, from 0 to 100, 0 =
								// logical, 100 = confused

	/**
	 * Progression Values
	 */

	public int key; // The key, this defines the root of the chord progression
	public int[] scaleType; // Major, minor, etc(?)
	public int[] chordProgression; // The chord progression
	public int currentMelody; // Each progression consists of four melodies (16
								// measures total), it's incremented by 1 every
								// time a melody is generated
	public int start;

	/**
	 * Effect Values
	 */

	public int bitCrusherCrush;
	public int bitCrusherDepth;
	public int reverbMix;
	public int reverbRoom;
	public int reverbDamping;
	public int globalVolume;
	public int tempo;

	/**
	 * Synth Values
	 */

	public int[] synth; // The main melody, higher synth
	public float[] synthvel; // The main melody, higher synth velocity
	public int synthAttack;
	public int synthDecay;
	public int synthSustain;
	public int synthRelease;
	public int synthWaveform;
	public int synthGlissando;
	public int synthVibratoDepth;
	public int synthVibratoSpeed;
	public int synthVibratoWaveform;
	public int synthTremeloDepth;
	public int synthTremeloWaveform;

	/**
	 * Bass Values
	 */

	public int[] bass; // Bass line, the lower synth
	public float[] bassvel; // Bass line, the lower synth velocity
	public int bassAttack;
	public int bassDecay;
	public int bassSustain;
	public int bassRelease;
	public int bassWaveform;
	public int bassGlissando;
	public int bassVibratoDepth;
	public int bassVibratoSpeed;
	public int bassVibratoWaveform;
	public int bassTremeloDepth;
	public int bassTremeloWaveform;

	/**
	 * Drum Values
	 */

	public int[] snare; // Snare drum
	public float[] snarevel; // Snare drum velocity
	public int snareSound;

	public int[] kick; // Kick (Bass) drum
	public float[] kickvel; // Kick (Bass) drum velocity
	public int kickSound;

	public int[] hihat; // Hi-hat
	public float[] hihatvel; // Hi-hat velocity
	public int hihatSound;

	public int drumsVolume;

	/**
	 * MelodyGenerator Constructor
	 * 
	 * @param h
	 *            - The happiness parameter
	 * @param e
	 *            - The excitement parameter
	 * @param c
	 *            - The confusion parameter
	 * @throws Exception
	 */

	MelodyGeneratorJoe() throws Exception {
		try {
			synth = new int[64];
			bass = new int[64];
			snare = new int[64];
			kick = new int[64];
			hihat = new int[64];

			synthvel = new float[64];
			bassvel = new float[64];
			snarevel = new float[64];
			kickvel = new float[64];
			hihatvel = new float[64];

			chordProgression = new int[4];
			scaleType = new int[4];

			loadFromFile();
		} catch (Exception e) {
			fileRead = false;
		}
	}

	MelodyGeneratorJoe(int h, int e, int c) throws Exception {

		happiness = h;
		excitement = e;
		confusion = c;

		/**
		 * Melody is generated 4 measures at a time, each value in the
		 * respective array represents the MIDI value to be played at that time
		 * (0-127, -1 for rest), drums are defined 0 for rest, 1 for played.
		 * Each value is worth one 16th note in 4/4 time.
		 */

		key = 1;
		try {
			prev = new MelodyGeneratorJoe();
		} catch (Exception exception) {
			fileRead = false;
		}

		synth = new int[64];
		bass = new int[64];
		snare = new int[64];
		kick = new int[64];
		hihat = new int[64];

		synthvel = new float[64];
		bassvel = new float[64];
		snarevel = new float[64];
		kickvel = new float[64];
		hihatvel = new float[64];

		chordProgression = new int[4];
		scaleType = new int[4];

		boolean up = false;

		for (int i = 0; i < 64; i++) {

			if (i % 8 == 0)
				up = !up;

			if (up) {
				synth[i] = i % 8;
				bass[i] = i % 8;
				snare[i] = 1;
				kick[i] = 1;
				hihat[i] = 1;
			} else {
				synth[i] = 7 - (i % 8);
				bass[i] = 7 - (i % 8);
				snare[i] = 1;
				kick[i] = 1;
				hihat[i] = 1;
			}

			synthvel[i] = .5f;
			bassvel[i] = .5f;
			snarevel[i] = .5f;
			kickvel[i] = .5f;
			hihatvel[i] = .5f;

			if (i < 4) {
				chordProgression[i] = 0;
				scaleType[i] = 0;
			}
		}
	}

	/**
	 * When this function is called, it takes care of all the work of generating
	 * music.
	 * 
	 * @throws Exception
	 */

	public void generateMelody() throws Exception {
		if (!DEBUG) {
			generateKey();
			generateScale();
			generateProgression();
			generateSynth();
			generateBass();
			generateDrums();
			transpose();
		} else {
			generateProgression();
			generateDrums();
			transpose();

		}
		try {
			saveToFile();
		} catch (Exception e) {
			System.out.println("Error Saving");
		}
	}

	/**
	 * Choose which key to be played, the key can range from 0 to 24 (C5 to C7
	 * for synth) 0 = C, 1 = C#, 2 = D, 3 = D#, 4 = E, 5 = F, 6 = F#, 7 = G, 8 =
	 * G#, 9 = A, 10 = A#, 11 = B
	 */

	public void generateKey() {
		double random = Math.random();
		if (!fileRead) { // If we can't read the melody generated from before
			key = 0;
		}
		if (fileRead && currentMelody == 1) { // If we're on a new
												// progression...
			if (excitement - prev.excitement >= 5) { // Things are getting more
														// exciting
				if (happiness - prev.happiness >= 3) { // Things are getting
														// happier
					key++;
				} else if (happiness - prev.happiness <= -3) { // Things are
																// getting
																// sadder
					key = key - 2;
				}
			}
		}
	}

	/**
	 * Choose which scale (major or minor) to be played
	 */

	public void generateScale() {
		/*
		 * if (happiness > 85) scaleType = 0; else if (happiness > 71) scaleType
		 * = 1; else if (happiness > 57) scaleType = 2; else if (happiness > 42)
		 * scaleType = 3; else if (happiness > 28) scaleType = 4; else if
		 * (happiness > 14) scaleType = 5; else scaleType = 6;
		 */
	}

	/**
	 * Generates the progression
	 * 
	 * Some popular 4 chord progressions: I - IV - V - V I - I - IV - V I - IV -
	 * I - V I - IV - V - IV
	 * 
	 * NOTE: I = 0, IV = 3, and such, it'll make transposing much easier.
	 */

	public void generateProgression() {
		double random = Math.random()*100;
		if (!fileRead || currentMelody == 1) {
			if (happiness > 66) { // Things are pretty happy!
				chordProgression[0] = 0; // Always start with the root
				scaleType[0] = 1; // Major scale 
				
				// Generate a semi-random progressions of I, IV, and V
				for (int i = 1; i < 4; i++) {
					double chordPicker = Math.random()*100;
					if (i == 1) {
						if (chordPicker > 50)
							chordProgression[i] = 0;
						else
							chordProgression[i] = 4;
					} else {
						if (chordPicker < 33) {
							chordProgression[i] = 0;
						} else if (chordPicker < 66) {
							chordProgression[i] = 4;
						} else {
							chordProgression[i] = 6;
						}
					}
					scaleType[i] = 1; // All major scales
				}

			}
			else if (happiness > 33){
				chordProgression[0] = 0; // Always start with the root
				scaleType[0] = 0;
				for(int i = 1; i < 4; i++) {
					double chordPicker = Math.random()*100;
					if(chordPicker < 25){
						chordProgression[i] = 2;
						if(Math.random() > 50)
							scaleType[i] = 4;
						else
							scaleType[i] = 6;
					}
					else if(chordPicker < 50){
						chordProgression[i] = 4;
						if(Math.random() > .5)
							scaleType[i] = 4;
						else
							scaleType[i] = 0;
					}
					else if(chordPicker < 75){
						chordProgression[i] = 0;
						scaleType[i] = 0;
					}
					else{
						chordProgression[i] = 6;
						scaleType[i] = 0;
					}
				}
			}
			else{
				for(int i = 0; i < 4; i++){
					double chordPicker = Math.random()*100;
					if(chordPicker < 20){
						chordProgression[i] = 0;
					}
					else if(chordPicker < 40){
						chordProgression[i] = 2;
					}
					else if(chordPicker < 60){
						chordProgression[i] = 4;
					}
					else if(chordPicker < 80){
						chordProgression[i] = 6;
					}
					else{
						if(Math.random() > .5){
							chordProgression[i] = 1;
						}
						else{
							chordProgression[i] = 3;
						}
					}
					scaleType[i] = (int)(Math.random()*7);
				}
			}
		}
		else{
			for(int i = 0; i < 4; i++)
				chordProgression[i] = prev.chordProgression[i];
		}
	}

	/**
	 * Generate the synth line
	 */
	public void generateSynth() {
		// synth = A sweet line
		int i;
		for (i = 0; i < 16; i++) { // Logic for first measure
			double noteChooser = Math.random();
			if (i % 2 == 0) { // Logic for eighth notes
				if (noteChooser < .4) {
					if (noteChooser < .05)
						synth[i] = 0;
					else if (noteChooser < .1)
						synth[i] = 1;
					else if (noteChooser < .15)
						synth[i] = 2;
					else if (noteChooser < .2)
						synth[i] = 3;
					else if (noteChooser < .25)
						synth[i] = 4;
					else if (noteChooser < .3)
						synth[i] = 5;
					else if (noteChooser < .35)
						synth[i] = 6;
					else if (noteChooser < .4)
						synth[i] = 7;
				}
				else
					synth[i] = -1;
			}
			else
				synth[i] = -1;
			if (i % 4 == 0) { // Logic for quarter notes
				if (noteChooser < .1)
					synth[i] = 0;
				else if (noteChooser < .2)
					synth[i] = 1;
				else if (noteChooser < .3)
					synth[i] = 2;
				else if (noteChooser < .4)
					synth[i] = 3;
				else if (noteChooser < .5)
					synth[i] = 4;
				else if (noteChooser < .6)
					synth[i] = 5;
				else if (noteChooser < .7)
					synth[i] = 6;
				else if (noteChooser < .8)
					synth[i] = 7;
			} 
			if (i % 8 == 0) { // Logic for beats 1 and 3
				if (noteChooser < .33)
					synth[i] = 0;
				else if (noteChooser < .66)
					synth[i] = 2;
				else if (noteChooser < 1)
					synth[i] = 4;
			} 
			if (i == 0 || i == 16 && Math.random() > .1) // Logic for the first note
				synth[i] = 0;
		}
		for (i = 48; i < 61; i++) { // Logic for fourth measure (as is, mirror of the first)
			synth[i] = synth[12 - (i - 48)];
		}
		for (i = 0; i < 16; i++) { // Elongating each note at random
			if (synth[i] == -1 && Math.random() > .4)
				synth[i] = synth[i - 1];
		}
		for (i = 16; i < 32; i++) { // Logic for second measure (as is, same as first)
			synth[i] = synth[i - 16];
		}
		for (i = 32; i < 48; i++) { // Logic for third measure (as is, same as first)
			synth[i] = synth[i - 32];
		}
		synth[63] = -1; // Guarantees that the last note is always a rest
	}

	/**
	 * Generate the bass line
	 */
	public void generateBass() {
		// bass = A sick groove
		for (int i = 0; i < 64; i++) {
			if (synth[i] != -1 && i % 2 == 0) {
				if (i % 4 == 0)
					bass[i] = synth[i];
				else if (Math.random() > 0.75)
					bass[i] = synth[i];
				else 
					bass[i] = -1;
			}
			else
				bass[i] = -1;
		}
		for (int i = 1; i < 64; i++) {
			if (bass[i] == -1)
				bass[i] = bass [i - 1];
		}
			
				
			/**	
			if (i == 0 && Math.random() > .1) { // Logic for the first note
				synth[0] = 0;
			}
			if (i != 0 && i % 2 == 0) { // Logic for eighth notes

				// if(i % 4 == 0){
				if (Math.random() * happiness > 20) {
					double noteChooser = Math.random();
					if (noteChooser < .25)
						synth[1] = 0;
					if (noteChooser > .25 && noteChooser < .5)
						synth[1] = 2;
					if (noteChooser > .5 && noteChooser < .75)
						synth[1] = 4;
					if (noteChooser > .75)
						synth[1] = 6;
				}
			}
			else {
				synth[i] = (int) (Math.random() * 7);
			}
			}
			**/
	}

	/**
	 * Generate the hihat, snare, and kick
	 */
	public void generateDrums() {
		generateHihat();
		generateKick();
		generateSnare();

	}

	public void generateHihat() {
		for (int i = 0; i < 64; i++) {
			if (excitement < 25) {
				if (i % 4 == 0)
					hihat[i] = 0;
				else 
					hihat[i] = 0;
			}
			else if (excitement < 50) {
				if (i % 2 == 0)
					hihat[i] = 1;
				else
					hihat[i] = 0;
			}
			else if (excitement < 75) {
				if (i % 4 == 2)
					hihat[i] = 1;
				else
					hihat[i] = 0;
			}
			else 
				if (i % 2 == 0)
					hihat[i] = 1;
				else
					hihat[i] = 0;
		}
		for (int i = 1; i < 64; i += 2) {
			if (Math.random() > .75)
				hihat[i] = 1;
		}
	}

	public void generateSnare() {
		for (int i = 0; i < 64; i++) {
			if (i % 4 == 0 && i % 8 != 0)
				snare[i] = 1;
			else
				snare[i] = 0;
		}
	}

	public void generateKick() {
		for (int i = 0; i < 64; i++) {
			if (i % 8 == 0)
				kick[i] = 1;
			else
				kick[i] = 0;
			if (i % 4 == 0 && Math.random() > 0.5)
				kick[i] = 1;
			if (Math.random() > 0.5)
				kick[19] = 1;
			if (Math.random() > 0.5)
				kick[35] = 1;
			if (Math.random() > 0.5)
				kick[51] = 1;
			if (Math.random() > 0.5)
				kick[6] = 1;
			if (Math.random() > 0.5)
				kick[22] = 1;
		}
	}

	/**
	 * Takes notes 0-7 in the instrument arrays and transposes everything into
	 * values 0 - 127 representing the correct actual MIDI note to be played,
	 * this is for synth and bass only.
	 */
	public void transpose() {
		int[][] scaleValues = new int[][] { { 0, 2, 4, 5, 7, 9, 11, 12 },
				{ 0, 2, 4, 5, 7, 9, 11, 12 }, { 0, 2, 4, 5, 7, 9, 11, 12 },
				{ 0, 2, 4, 5, 7, 9, 11, 12 } }; // Parallel array that will
												// transpose notes 0 - 7 into a
												// diatonic scale

		for (int i = 0; i < 4; i++) {
			if (scaleType[i] == 0)
				scaleValues[i] = new int[] { 0, 2, 4, 6, 7, 9, 11, 12 }; // Lydian
																			// -
																			// "brightest"
			if (scaleType[i] == 1)
				scaleValues[i] = new int[] { 0, 2, 4, 5, 7, 9, 11, 12 }; // Ionian
																			// (major)
			if (scaleType[i] == 2)
				scaleValues[i] = new int[] { 0, 2, 4, 5, 7, 9, 10, 12 }; // Mixolydian
			if (scaleType[i] == 3)
				scaleValues[i] = new int[] { 0, 2, 3, 5, 7, 9, 10, 12 }; // Dorian
			if (scaleType[i] == 4)
				scaleValues[i] = new int[] { 0, 2, 3, 5, 7, 8, 10, 12 }; // Aeolian
																			// (natural
																			// minor)
			if (scaleType[i] == 5)
				scaleValues[i] = new int[] { 0, 1, 3, 5, 7, 8, 10, 12 }; // Phrygian
			if (scaleType[i] == 6)
				scaleValues[i] = new int[] { 0, 1, 3, 5, 6, 8, 10, 12 }; // Locrian
																			// -
																			// "darkest"
		}

		int c = -1; // Current place in the progression that the note will be
					// transposed to

		for (int i = 0; i < 64; i++) {
			if (i % 16 == 0) // Current place in chord progression
				c++;

			if (synth[i] != -1) { // Unless it's a rest, transpose the raw 0 - 7
									// into the proper note within a scale
				synth[i] = scaleValues[c][synth[i]];
				synth[i] += (chordProgression[c] + 12 * 5); // Move the note up
															// to the proper
															// scale and octave
			}

			if (bass[i] != -1) {
				bass[i] = scaleValues[c][bass[i]];
				bass[i] += (chordProgression[c] + 12 * 3); // Move the note up
															// to the proper
															// scale and octave
			}
		}
	}

	public void saveToFile() throws IOException {
		try {
			FileWriter fstream = new FileWriter("melody.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			StringBuilder output = new StringBuilder();

			output.append(happiness + " ");
			output.append(excitement + " ");
			output.append(confusion + " ");
			output.append(key + " ");

			for (int i = 0; i < 4; i++) {
				output.append(chordProgression[i] + " ");
				output.append(scaleType[i] + " ");
			}
			output.append(start + " ");
			output.append(bitCrusherCrush + " ");
			output.append(bitCrusherDepth + " ");
			output.append(reverbMix + " ");
			output.append(reverbDamping + " ");
			output.append(globalVolume + " ");
			output.append(tempo + " ");

			output.append(synthAttack + " ");
			output.append(synthDecay + " ");
			output.append(synthSustain + " ");
			output.append(synthRelease + " ");
			output.append(synthWaveform + " ");
			output.append(synthGlissando + " ");
			output.append(synthVibratoDepth + " ");
			output.append(synthVibratoSpeed + " ");
			output.append(synthVibratoWaveform + " ");
			output.append(synthTremeloDepth + " ");
			output.append(synthTremeloWaveform + " ");

			output.append(bassAttack + " ");
			output.append(bassDecay + " ");
			output.append(bassSustain + " ");
			output.append(bassRelease + " ");
			output.append(bassWaveform + " ");
			output.append(bassGlissando + " ");
			output.append(bassVibratoDepth + " ");
			output.append(bassVibratoSpeed + " ");
			output.append(bassVibratoWaveform + " ");
			output.append(bassTremeloDepth + " ");
			output.append(bassTremeloWaveform + " ");

			output.append(snareSound + " ");
			output.append(kickSound + " ");
			output.append(hihatSound + " ");
			output.append(drumsVolume + " ");

			for (int i = 0; i < 64; i++) {
				output.append(synth[i] + " " + bass[i] + " " + snare[i] + " "
						+ kick[i] + " " + hihat[i] + " "
						+ (int) (synthvel[i] * 100) + " " /* To save as an int */
						+ (int) (bassvel[i] * 100) + " "
						+ (int) (snarevel[i] * 100) + " "
						+ (int) (kickvel[i] * 100) + " "
						+ (int) (hihatvel[i] * 100) + " ");
			}

			out.write(output.toString());
			System.out.println("Success");
			out.close();
		} catch (IOException e) {

		}
	}

	public void loadFromFile() throws FileNotFoundException {
		try {
			FileReader fstream = new FileReader("melody.txt");
			Scanner in = new Scanner(fstream);
			happiness = in.nextInt(); // 1
			excitement = in.nextInt(); // 2
			confusion = in.nextInt(); // 3
			key = in.nextInt(); // 4

			for (int i = 0; i < 4; i++) {
				chordProgression[i] = in.nextInt(); // 5, 7, 9, 11
				scaleType[i] = in.nextInt(); // 6, 8, 10, 12
			}
			start = in.nextInt(); // 13
			bitCrusherCrush = in.nextInt(); // 14
			bitCrusherDepth = in.nextInt(); // 15
			reverbMix = in.nextInt(); // 16
			reverbDamping = in.nextInt(); // 17
			globalVolume = in.nextInt(); // 18
			tempo = in.nextInt(); // 19

			synthAttack = in.nextInt();
			synthDecay = in.nextInt();
			synthSustain = in.nextInt();
			synthRelease = in.nextInt();
			synthWaveform = in.nextInt();
			synthGlissando = in.nextInt();
			synthVibratoDepth = in.nextInt();
			synthVibratoSpeed = in.nextInt();
			synthVibratoWaveform = in.nextInt();
			synthTremeloDepth = in.nextInt();
			synthTremeloWaveform = in.nextInt();

			bassAttack = in.nextInt();
			bassDecay = in.nextInt();
			bassSustain = in.nextInt();
			bassRelease = in.nextInt();
			bassWaveform = in.nextInt();
			bassGlissando = in.nextInt();
			bassVibratoDepth = in.nextInt();
			bassVibratoSpeed = in.nextInt();
			bassVibratoWaveform = in.nextInt();
			bassTremeloDepth = in.nextInt();
			bassTremeloWaveform = in.nextInt();

			snareSound = in.nextInt();
			kickSound = in.nextInt();
			hihatSound = in.nextInt();
			drumsVolume = in.nextInt();

			for (int i = 0; i < 64; i++) {
				synth[i] = in.nextInt();
				bass[i] = in.nextInt();
				snare[i] = in.nextInt();
				kick[i] = in.nextInt();
				hihat[i] = in.nextInt();
				synthvel[i] = (float) (in.nextInt() / 100);
				bassvel[i] = (float) (in.nextInt() / 100);
				snarevel[i] = (float) (in.nextInt() / 100);
				kickvel[i] = (float) (in.nextInt() / 100);
				hihatvel[i] = (float) (in.nextInt() / 100);
			}
			fileRead = true;
		}

		catch (FileNotFoundException e) {
			fileRead = false;
		}
	}
};