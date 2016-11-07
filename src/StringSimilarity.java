/**
 * Compute the similarity between two strings based on the length
 * of the longest string and the Levenshtein Edit Distance.
 * Not very efficient when used to compare a large number of
 * large strings.
 * Implementation by acdcjunior on StackOverflow.
 */
public class StringSimilarity
{
  /**
   * Calculates the similarity (a number within 0 and 1) between two strings.
   * @param s1 - one string
   * @param s2 - another string
   * @return Similarity measure (0 to 1)
   */
  public static double similarity(String s1, String s2) {
    String longer = s1, shorter = s2;
    if (s1.length() < s2.length()) { // longer should always have greater length
      longer = s2; shorter = s1;
    }
    int longerLength = longer.length();
    if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
    /* // If you have StringUtils, you can use it to calculate the edit distance:
    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                               (double) longerLength; */
    return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
  }

  /**
   * Example implementation of the Levenshtein Edit Distance
   * (minimum number of single-character edits [insertions,
   * deletions or substitutions]) required to change one string
   * into another.
   * @param s1 - one string
   * @param s2 - another string
   * @return Edit distance between strings
   */
  // See http://rosettacode.org/wiki/Levenshtein_distance#Java
  public static int editDistance(String s1, String s2) {
    //s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();

    int[] costs = new int[s2.length() + 1];
    for (int i = 0; i < s1.length(); i++)
    {
      int lastValue = i;
      char s1Char = Character.toLowerCase(s1.charAt(i));
      for (int j = 0; j < s2.length(); j++)
      {
        if (i == 0)
        {
          costs[j] = j;
        }
        else
        {
          if (j > 0)
          {
            int newValue = costs[j - 1];
            if (s1Char != s2.charAt(j - 1))
              newValue = Math.min(Math.min(newValue, lastValue),
                  costs[j]) + 1;
            costs[j - 1] = lastValue;
            lastValue = newValue;
          }
        }
      }
      if (i > 0)
      {
        costs[s2.length() - 1] = lastValue;
      }
    }
    return costs[s2.length() - 1];
  }
}