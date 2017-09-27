package com.teamtreehouse.dbanalysis;

import com.teamtreehouse.dbanalysis.model.Country;
import com.teamtreehouse.dbanalysis.model.Country.CountryBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.*;
import java.util.regex.Pattern;

public class Application {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException {
        LinkedHashMap<String, String> mMenu = new LinkedHashMap<String, String>();

        //Create menu options
        mMenu.put("display", "Display data in a table format.");
        mMenu.put("stats", "View statistics such as max, min, and correlation coefficient.");
        mMenu.put("edit", "Edit information for a country.");
        mMenu.put("add", "Add a country to the database.");
        mMenu.put("delete", "Delete a country from the database.");
        mMenu.put("quit", "Exit the program.");

        String choice;
        do {
            choice = promptAction(mMenu);
            switch (choice) {
                case "display":
                    displayTable(fetchAllCountries());
                    break;
                case "stats":
                    displayStats(fetchAllCountries());
                    break;
                case "edit":
                    updateCountryInfo();
                    break;
                case "add":
                    addCountry();
                    break;
                case "delete":
                    deleteCountry();
                    break;
                case "quit":
                    System.out.println("Now quiting program... Goodbye.");
                    break;
                default:
                    System.out.printf("Unknown choice:  '%s'. Try again.  %n%n%n",
                            choice);

            }
        } while (!choice.equals("quit"));
    }

    private static void displayStats(List<Country> countries) {
        System.out.printf("%n%nHere is a list of all the stats:%n%n");
        // Internet Users
        maxInternet(countries);
        notNullMaxInternet(countries);

        minInternet(countries);
        notNullMinInternet(countries);

        // Literacy rates
        maxLiteracy(countries);
        notNullmaxLiteracy(countries);

        minLiteracy(countries);
        notNullminLiteracy(countries);

        // Correlation Coefficient
        corrCoef();
    }


    private static String promptAction(LinkedHashMap<String, String> mMenu) {
        Scanner scanner = new Scanner(System.in);
        for (Map.Entry<String, String> option : mMenu.entrySet()) {
            System.out.printf("%s - %s %n",
                    option.getKey(),
                    option.getValue());
        }
        System.out.print("Welcome to the WorldBank Database. What do you want to do: ");
        String choice = scanner.nextLine();
        return choice.trim().toLowerCase();
    }

    private static void update(Country country) {
        // open a session
        Session session = sessionFactory.openSession();

        // begin a transaction
        session.beginTransaction();

        // use the session to update the contact
        session.update(country);

        // commit the transaction
        session.getTransaction().commit();

        // close the session
        session.close();
    }

    public static void delete(Country country) {
        // open a session
        Session session = sessionFactory.openSession();

        //begin a transaction
        session.beginTransaction();

        // use the session to delete the contact
        session.delete(country);

        //commit the transaction
        session.getTransaction().commit();

        //close session
        session.close();
    }

    private static void save(Country country ) {
        // Open a session
        Session session = sessionFactory.openSession();

        // Begin a transaction
        session.beginTransaction();

        // Use the session to save the contact
        session.save(country);

        //commit the transaction
        session.getTransaction().commit();

        //close the session
        session.close();
    }


    private static List<Country> fetchAllCountries() {
        // Open a session
        Session session = sessionFactory.openSession();

        // Create criteria object
        Criteria criteria = session.createCriteria(Country.class);

        //Get a list of Contact objects according to the Criteria object
        List<Country> countries = criteria.list();
        for(Country c : countries) {
            if(c.getInternetUsers() == null) c.setInternetUsers(0.00);
            if(c.getAdultLiteracyRate() == null) c.setAdultLiteracyRate(0.00);
        }

        // Close the session
        session.close();

        return countries;
    }

    private static void deleteCountry() {
        Scanner scanner = new Scanner(System.in);
        Country c = null;
        printCountriesByName();
        c = promptForCountry(scanner);
        if (c == null) System.out.printf("%nExiting...%n");
        printCountry(c);
        System.out.printf("%n%n(yes or no) Would you like to delete the above country?%n");
        String answer = scanner.nextLine();
        if (answer.trim().equalsIgnoreCase("yes")) {
            delete(c);
            System.out.printf("%n%nCountry deleted.%n%n");
        }
    }

    private static void addCountry() {
        Scanner scanner = new Scanner(System.in);
        System.out.printf("%nLet's add a country.%n");
        boolean haveName = false;
        boolean haveInternet = false;
        boolean haveLiteracy = false;
        boolean haveCode = false;
        String countryCode;
        String countryName;
        double doubleCountryInternet = 0.00;
        double doubleLiteracyRate = 0.00;
        do {
        System.out.printf("%nPlease provide a country name (String): %n");
        countryName = scanner.nextLine();
        if (!nameAvailable(countryName)) {
            System.out.printf("%nI am sorry, %s is already taken.%n", countryName);
        } else if (countryName.trim().length() > 1 && nameAvailable(countryName)){
            haveName = true;
        } else {
            tryAgain(countryName);
        }
        } while (!haveName);
        do {
        System.out.printf("%nPlease provide the internet users number (double), if not known enter 0.00: %n");
        String countryInternet = scanner.nextLine();
        if (canBeDouble(countryInternet)) {
            doubleCountryInternet = new Double(countryInternet);
            haveInternet = true;
        } else {
            tryAgain(countryInternet);
        }
        } while (!haveInternet);
        do {
        System.out.printf("%nPlease provide the literacy rate number (double), if not known enter 0.00: %n");
        String countryLiteracy = scanner.nextLine();
        if (canBeDouble(countryLiteracy)) {
            doubleLiteracyRate = new Double(countryLiteracy);
            haveLiteracy = true;
        } else {
            tryAgain(countryLiteracy);
        }
        } while (!haveLiteracy);
        do {
        System.out.printf("%nPlease provide a country code (String, 3 in length): %n");
        countryCode = scanner.nextLine();
        if (countryCode.trim().length() == 3 && codeAvailable(countryCode)) {
            haveCode = true;
        } else if (countryCode.trim().length() != 3) {
            System.out.printf("%nCountry code must be three characters in length. You entered: %s%n",
                    countryCode);
        } else if (!codeAvailable(countryCode)) {
            System.out.printf("I am sorry, %s is already taken.", countryCode);
        } else {
            tryAgain(countryCode);
        }
        } while (!haveCode);
        if (haveName && haveCode && haveInternet && haveLiteracy) {
            System.out.printf("%n%nAdding country...%n%n");
            Country country = new CountryBuilder(countryCode, countryName)
                    .withInternetUsers(doubleCountryInternet)
                    .withAdultLiteracyRate(doubleLiteracyRate)
                    .build();
            save(country);
            System.out.printf("%nCountry added as:%n");
            printCountry(country);
        }
    }

    private static boolean nameAvailable(String countryName) {
        List<Country> countries = fetchAllCountries();
        for (Country c : countries) {
            if (c.getName().equalsIgnoreCase(countryName.trim())) return false;
        }
        return true;
    }

    private static void updateCountryInfo() {
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        Country c;
        do {
            // print all country names
            printCountriesByName();
            c = promptForCountry(scanner);
            if (c == null) {
                done = true;
            }
            System.out.printf("%nSelected Country:%n");
            printCountry(c);
            String change = promptForChange(scanner);
            if (change.trim().toLowerCase().equals("cancel")) {
                done = true;
            }
            if (change.trim().toLowerCase().equals("name")) {
                promptForNewName(scanner, c);
                update(c);
                done = true;
            }
            if (change.trim().toLowerCase().equals("internet users")) {
                promptForNewInternet(scanner, c);
                update(c);
                done = true;
            }
            if (change.trim().toLowerCase().equals("literacy rate")) {
                promptForNewLiteracy(scanner, c);
                update(c);
                done = true;
            }
            if (change.trim().toLowerCase().equals("code")) {
                promptForNewCode(scanner, c);
                update(c);
                done = true;
            }
        } while (!done);
    }

    private static Country promptForCountry(Scanner scanner) {
        boolean done = false;
        Country country = null;
        do {
            System.out.printf("%nWhich country would you like to select? or cancel%n");
            String updateCountry = scanner.nextLine();
            for ( Country c : fetchAllCountries()) {
                if (c.getName().equalsIgnoreCase(updateCountry.trim())) {
                    country = c;
                    done = true;
                } else if (updateCountry.trim().equalsIgnoreCase("cancel")) {
                    done = true;
                }
            } if (!done) tryAgain(updateCountry);
        } while(!done);
        return country;
    }

    private static String promptForChange(Scanner scanner) {
        boolean done = false;
        String change = "";

        do {
            System.out.printf("%nWould you like to change the name, internet users, literacy rate, code, or cancel?%n");
            String changeWhat = scanner.nextLine();
            if (changeWhat.trim().equalsIgnoreCase("name")) {
                change = "name";
                done = true;
            } else if (changeWhat.trim().equalsIgnoreCase("internet users")) {
                change = "internet users";
                done = true;
            } else if (changeWhat.trim().equalsIgnoreCase("literacy rate")) {
                change = "literacy rate";
                done = true;
            } else if (changeWhat.trim().equalsIgnoreCase("code")) {
                change = "code";
                done = true;
            } else if (changeWhat.trim().equalsIgnoreCase("cancel")) {
                change = "cancel";
                done = true;
            }
            if (!done) tryAgain(changeWhat);
        } while (!done);
        return change;
    }

    private static void promptForNewName(Scanner scanner, Country country) {
        boolean done = false;
        do {
            System.out.printf("%n%n What would you like to change the name to? or cancel%n%n");
            String name = scanner.nextLine();
            if (name.trim().equalsIgnoreCase("cancel")) {
                done = true;
            } else if (!name.equals("")) {
                System.out.printf("%n%nChanging %s name to %s...%n%n", country.getName(), name);
                country.setName(name);
                printCountry(country);
                done = true;
            }
            if (!done) tryAgain(name);
        } while (!done);
    }

    private static void promptForNewInternet(Scanner scanner, Country country) {
        boolean done = false;
        System.out.printf("Current internet users: %.02f", country.getInternetUsers());
        do {
            System.out.printf("%n%nWhat would you like to change internet users to? or cancel%n%n");
            String internet = scanner.nextLine();
            if (internet.trim().equalsIgnoreCase("cancel")) {
                done = true;
            } else if (canBeDouble(internet)) {
                System.out.printf("%n%nChanging internet users from: %s to %s...%n%n",
                        country.getInternetUsers(),
                        internet);
                double internetUsers = new Double(internet);
                country.setInternetUsers(internetUsers);
                printCountry(country);
                done = true;
            }
            if (!done) tryAgain(internet);
        } while (!done);

    }

    private static void promptForNewLiteracy(Scanner scanner, Country country) {
        boolean done = false;
        System.out.printf("Current literacy rate: %.02f", country.getAdultLiteracyRate());
        do {
            System.out.printf("%n%nWhat would you like to change the literacy rate to? or cancel%n%n");
            String literacy = scanner.nextLine();
            if (literacy.trim().equalsIgnoreCase("cancel")) {
                done = true;
            } else if (canBeDouble(literacy)) {
                System.out.printf("%n%nChanging literacy rate from: %s to: %s",
                        country.getAdultLiteracyRate(),
                        literacy);
                double literacyRate = new Double(literacy);
                country.setAdultLiteracyRate(literacyRate);
                printCountry(country);
                done = true;
            }
            if (!done) tryAgain(literacy);
        } while (!done);
    }

    private static void promptForNewCode(Scanner scanner, Country country) {
        boolean done = false;
        do {
            System.out.printf("%n%n What would you like to change the code to? or cancel%n%n");
            String code = scanner.nextLine();
            if (code.trim().equalsIgnoreCase("cancel")) {
                done = true;
            }
            if (codeAvailable(code) && code.trim().length() == 3) {
                System.out.printf("%n%nChanging %s code to %s...%n%n", country.getCode(), code);
                country.setCode(code);
                printCountry(country);
                done = true;
            } else if (code.trim().length() != 3 && !code.equalsIgnoreCase("cancel")) {
                System.out.printf("I am sorry the code must be three characters long, you entered: %s", code);
            } else  if (!codeAvailable(code)){
                System.out.printf("%n%nI am sorry, %s is already in use. Please try again.%n%n", code);
            } else {
                if (!done) tryAgain(code);
            }
        } while (!done);
    }

    private static void printCountriesByName() {
        System.out.printf("Here is a list of all the countries:%n%n");
        List<Country> countries = fetchAllCountries();
        countries.sort(Comparator.comparing(Country::getName));
        for (Country name : countries) {
            System.out.println(name.getName() + " - " + name.getCode());
        }
    }

    private static boolean codeAvailable(String code) {
        List<Country> countries = fetchAllCountries();
        for (Country c : countries) {
            if (c.getCode().equalsIgnoreCase(code.trim())) return false;
        }
        return true;
    }


    private static void printCountry(Country country) {
        if (country.getInternetUsers() != 0.00 && country.getAdultLiteracyRate() != 0.00) {
            System.out.printf("%n%n Name: %s, Internet Users: %.02f, Literacy: %.02f, Code: %s%n%n",
                    country.getName(),
                    round(country.getInternetUsers(), 2),
                    round(country.getAdultLiteracyRate(), 2),
                    country.getCode());
        }
        if (country.getInternetUsers() == 0.00 && country.getAdultLiteracyRate() != 0.00) {
            String internetUsers = "--";
            System.out.printf("%n%n Name: %s, Internet Users: %s, Literacy: %.02f, Code: %s%n%n",
                    country.getName(),
                    internetUsers,
                    round(country.getAdultLiteracyRate(), 2),
                    country.getCode());
        }
        if (country.getInternetUsers() != 0.00 && country.getAdultLiteracyRate() == 0.00) {
            String literacyRate = "--";
            System.out.printf("%n%n Name: %s, Internet Users: %.02f, Literacy: %s, Code: %s%n%n",
                    country.getName(),
                    round(country.getInternetUsers(), 2),
                    literacyRate,
                    country.getCode());
        }
        if (country.getInternetUsers() == 0.00 && country.getAdultLiteracyRate() == 0.00) {
            String internetUsers = "--";
            String literacyRate = "--";
            System.out.printf("%n%n Name: %s, Internet Users: %s, Literacy: %s, Code: %s%n%n",
                    country.getName(),
                    internetUsers,
                    literacyRate,
                    country.getCode());
        }
    }

    /*
     takes a string and checks format to see if it can be a double.
     returns true or false.
     */
    private static boolean canBeDouble(String string) {
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
                ("[\\x00-\\x20]*"+ // Optional leading "whitespace"
                        "[+-]?(" +         // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from the Java Language Specification, 2nd
                        // edition, section 3.10.2.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.("+Digits+")("+Exp+")?)|"+

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

            if (Pattern.matches(fpRegex, string)) {
                return true; // Will not throw NumberFormatException
            } else {
                return false;
        }
    }

    /*
    takes a string from user
    returns a message to try again
    */
    private static void tryAgain(String string) {
        System.out.println("I am sorry, I am not sure what you mean by " + string + ", try again.");
    }

    @SuppressWarnings("unchecked")
    /*
    takes a list of countries
    prints a well formatted table
     */
    private static void displayTable(List<Country> countries) {
        System.out.format("%-30s%-20s%-20s%n", "Country", "Internet Users", "Literacy");
        for (int i = 0; i < 80; i++) System.out.print("-");
        System.out.println();
        for (Country c : countries) {
            if (c.getInternetUsers() != 0.00 && c.getAdultLiteracyRate() != 0.00) {
                System.out.format("%-30s%-20.02f%-20.02f%n",
                        c.getName(),
                        round(c.getInternetUsers(), 2),
                        round(c.getAdultLiteracyRate(), 2));
            }
            if (c.getInternetUsers() == 0.00 && c.getAdultLiteracyRate() != 0.00) {
                String internetUsers = "--";
                System.out.format("%-30s%-20s%-20.02f%n",
                        c.getName(),
                        internetUsers,
                        round(c.getAdultLiteracyRate(), 2));
            }
            if (c.getInternetUsers() != 0.00 && c.getAdultLiteracyRate() == 0.00) {
                String literacyRate = "--";
                System.out.format("%-30s%-20.02f%-20s%n",
                        c.getName(),
                        round(c.getInternetUsers(), 2),
                        literacyRate);
            }
            if (c.getInternetUsers() == 0.00 && c.getAdultLiteracyRate() == 0.00) {
                String internetUsers = "--";
                String literacyRate = "--";
                System.out.format("%-30s%-20s%-20s%n",
                        c.getName(),
                        internetUsers,
                        literacyRate);
            }
        }
    }

    private static void maxInternet(List<Country> countries) {
        Country max = countries.stream()
                .max(Comparator.comparing(Country::getInternetUsers))
                .get();

        if (max.getAdultLiteracyRate() != 0.00) {
            System.out.printf("%n%nCountry with the highest Internet Users:%n Name: %s, Internet Users: %.02f, Literacy: %.02f%n%n",
                    max.getName(),
                    round(max.getInternetUsers(), 2),
                    round(max.getAdultLiteracyRate(), 2));
        } else {
            System.out.printf("%n%nCountry with the highest Internet Users:%n Name: %s, Internet Users: %.02f, Literacy: %s%n%n",
                    max.getName(),
                    round(max.getInternetUsers(), 2),
                    "--");
        }

    }

    private static void notNullMaxInternet(List<Country> countries) {
        Country max = countries.stream()
                .filter(country -> country.getInternetUsers() != 0.00 && country.getAdultLiteracyRate() != 0.00)
                .max(Comparator.comparing(Country::getInternetUsers))
                .get();

        System.out.printf("%n%nCountry with the highest Internet Users and recorded Literacy Rate:%n Name: %s, Internet Users: %.02f, Literacy: %.02f%n%n",
                max.getName(),
                round(max.getInternetUsers(), 2),
                round(max.getAdultLiteracyRate(), 2));

    }

    private static void minInternet(List<Country> countries) {
        Country minCountry = countries.stream()
                .filter(country -> country.getInternetUsers() != 0.00)
                .min(Comparator.comparing(Country::getInternetUsers))
                .get();

        if (minCountry.getAdultLiteracyRate() != 0.00) {
            System.out.printf("%n%nCountry with the lowest Internet Users:%n Name: %s, Internet Users: %.02f, Literacy: %.02f%n%n",
                    minCountry.getName(),
                    round(minCountry.getInternetUsers(), 2),
                    round(minCountry.getAdultLiteracyRate(), 2));
        } else {
            System.out.printf("%n%nCountry with the lowest Internet Users:%n Name: %s, Internet Users: %.02f, Literacy: %s%n%n",
                    minCountry.getName(),
                    round(minCountry.getInternetUsers(), 2),
                    "--");
        }

    }

    private static void notNullMinInternet(List<Country> countries) {
        Country minCountry = countries.stream()
                .filter(country -> country.getAdultLiteracyRate() != 0.00 && country.getInternetUsers() != 0.00)
                .min(Comparator.comparing(Country::getInternetUsers))
                .get();

        System.out.printf("%n%nCountry with the lowest Internet Users and recorded Literacy Rate:%n Name: %s, Internet Users: %.02f, Literacy: %.02f%n%n",
                minCountry.getName(),
                round(minCountry.getInternetUsers(), 2),
                round(minCountry.getAdultLiteracyRate(), 2));

    }

    private static void maxLiteracy(List<Country> countries) {
        Country maxCountry = countries.stream()
                .max(Comparator.comparing(Country::getAdultLiteracyRate))
                .get();

        if (maxCountry.getInternetUsers() != 0.00) {
            System.out.printf("%n%nCountry with the highest Literacy Rate:%n Name: %s, Literacy Rate: %.02f, Internet Users: %.02f%n%n",
                    maxCountry.getName(),
                    round(maxCountry.getAdultLiteracyRate(), 2),
                    round(maxCountry.getAdultLiteracyRate(), 2));
        } else {
            System.out.printf("%n%nCountry with the highest Literacy Rate:%n Name: %s, Literacy Rate: %.02f, Internet Users: %s%n%n",
                    maxCountry.getName(),
                    round(maxCountry.getAdultLiteracyRate(), 2),
                    "--");
        }

    }

    private static void notNullmaxLiteracy(List<Country> countries) {
        Country maxCountry = countries.stream()
                .filter(country -> country.getInternetUsers() != 0.00 && country.getAdultLiteracyRate() != 0.00)
                .max(Comparator.comparing(Country::getAdultLiteracyRate))
                .get();

        System.out.printf("%n%nCountry with the highest Literacy Rate and recorded Internet Users:%n Name: %s, Literacy Rate: %.02f, Internet Users: %.02f%n%n",
                maxCountry.getName(),
                round(maxCountry.getAdultLiteracyRate(), 2),
                round(maxCountry.getAdultLiteracyRate(), 2));

    }

    private static void minLiteracy(List<Country> countries) {
        Country minCountry = countries.stream()
                .filter(country -> country.getAdultLiteracyRate() != 0.00)
                .min(Comparator.comparing(Country::getAdultLiteracyRate))
                .get();

        if (minCountry.getInternetUsers() != 0.00) {
            System.out.printf("%n%nCountry with the lowest Literacy Rate:%n Name: %s, Literacy Rate: %.02f, Internet Users: %.02f%n%n",
                    minCountry.getName(),
                    round(minCountry.getAdultLiteracyRate(), 2),
                    round(minCountry.getAdultLiteracyRate(), 2));
        } else {
            System.out.printf("%n%nCountry with the lowest Literacy Rate:%n Name: %s, Literacy Rate: %.02f, Internet Users: %s%n%n",
                    minCountry.getName(),
                    round(minCountry.getAdultLiteracyRate(), 2),
                    "--");
        }

    }

    private static void notNullminLiteracy(List<Country> countries) {
        Country minCountry = countries.stream()
                .filter(country -> country.getAdultLiteracyRate() != 0.00 && country.getInternetUsers() != 0.00)
                .min(Comparator.comparing(Country::getAdultLiteracyRate))
                .get();

        System.out.printf("%n%nCountry with the lowest Literacy Rate and recorded Internet Users:%n Name:%s, Literacy Rate: %.02f, Internet Users: %.02f%n%n",
                minCountry.getName(),
                round(minCountry.getAdultLiteracyRate(), 2),
                round(minCountry.getAdultLiteracyRate(), 2));

    }

    private static void corrCoef() {
        List<Country> noNullCountries = new ArrayList<>();

        for (Country c : fetchAllCountries()) {
            if (c.getInternetUsers() != 0.00 && c.getAdultLiteracyRate() != 0.00) noNullCountries.add(c);
        }
        double r,nr=0,dr_1=0,dr_2=0,dr_3=0,dr=0;
        double xx[], xy[], yy[];
        xx = new double[noNullCountries.size()];
        xy = new double[noNullCountries.size()];
        yy = new double[noNullCountries.size()];
        double x[], y[];
        x = new double[noNullCountries.size()];
        y = new double[noNullCountries.size()];
        double sum_y=0,sum_yy=0,sum_xy=0,sum_x=0,sum_xx=0;
        int i,n=noNullCountries.size();
        for (Country c : noNullCountries) {
            x[noNullCountries.indexOf(c)]= c.getInternetUsers();
            y[noNullCountries.indexOf(c)]= c.getAdultLiteracyRate();
        }
        for (i = 0; i < n; i++) {
            xx[i]=x[i]*x[i];
            yy[i]=y[i]*y[i];
        }
        for(i=0;i<n;i++) {
            sum_x+=x[i];
            sum_y+=y[i];
            sum_xx+= xx[i];
            sum_yy+=yy[i];
            sum_xy+= x[i]*y[i];
        }
        nr=(n*sum_xy)-(sum_x*sum_y);
        double sum_x2=sum_x*sum_x;
        double sum_y2=sum_y*sum_y;
        dr_1=(n*sum_xx)-sum_x2;
        dr_2=(n*sum_yy)-sum_y2;
        dr_3=dr_1*dr_2;
        dr=Math.sqrt(dr_3);
        r=(nr/dr);
        String s = String.format("%.2f",r);
        r = Double.parseDouble(s);
        System.out.println("\n\nTotal Number of Countries in database: "+n+"\nCorrelation Coefficient (Internet Users & Literacy Rates): "+r+"\n\n");
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
