package utils;

public final class Encryption {

    public static String encryptDecryptXOR(String rawString) {

        // If encryption is enabled in Config.
        if (Config.getEncryption()) {

            // The key is predefined and hidden in code
            // TODO: Create a more complex code and store it somewhere better     :FIX
//      char[] key = {'C', 'B', 'S'};
            char[] key = Config.getEncryptionCode().toCharArray();


            // Stringbuilder enables you to play around with strings and make useful stuff
            StringBuilder thisIsEncrypted = new StringBuilder();

            // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?  :FIX måske/gæt
            /**
             * Denne XOR bliver lavet i en "for loop", hvor dens antal loops er bestemt af hvor langt kodeordet er.
             * thisIsEncryption er en indbygget java klasse kaldet "StringBuilder", som bliver benytter til at lave og modifisere en streng.
             * Der bliver brugt metoden append (dansk = tilføj), som tager det en bogstavet vi er noget til i "for loopet" i parameter og tilføjer.
             * Inden det bliver tilføjet kommer det igennem selve krypteringen, hvor bogstavet først bliver " ^ " opløftet med arrayet "key", hvor
             * der bliver valgt et bogstav udfra operatoren "remainder", som ser hvor mange gange den kan gå op den og returnere
             * det der er tilovers, når den ikke kan gå helt op i "key" længden, 3 (CBS).
             *
             * Eksempel:
             * String kodeord/rawString = "abe";
             * char[] key = {'C', 'B', 'S'};
             * når i = 1
             * rawString.charAt( b ) ^ key[ 1 % 3 ]
             * rawString.charAt( b ) ^ 0
             * rawString.charAt( b )
             * thisIsEncrypted.append( b )
             */

            for (int i = 0; i < rawString.length(); i++) {
                thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
            }

            // We return the encrypted string
            return thisIsEncrypted.toString();

        } else {
            // We return without having done anything
            return rawString;
        }
    }
}
