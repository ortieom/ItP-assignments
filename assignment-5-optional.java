import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class Main {
    private Main() { }

    // constraints
    private static final int MINIMAL_DAYS = 1;
    private static final int MAXIMAL_DAYS = 30;
    private static final int MINIMAL_ANIMALS = 1;
    private static final int MAXIMAL_ANIMALS = 20;
    private static final int PARAMETERS_NUMBER = 4;

    // indexes of parameters
    private static final int TYPE_INDEX = 0;
    private static final int WEIGHT_INDEX = 1;
    private static final int SPEED_INDEX = 2;
    private static final int ENERGY_INDEX = 3;

    private static Scanner scanner;

    static {
        try {
            scanner = new Scanner(new File("input.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Animal receiveAnimal(String st, int id)
            throws InvalidNumberOfAnimalParametersException, InvalidInputsException, WeightOutOfBoundsException,
            SpeedOutOfBoundsException, EnergyOutOfBoundsException {
        String[] parameters = st.split(" ");

        if (parameters.length != PARAMETERS_NUMBER) {
            throw new InvalidNumberOfAnimalParametersException();
        }

        if (!(parameters[TYPE_INDEX].equals("Boar") || parameters[TYPE_INDEX].equals("Lion")
                || parameters[TYPE_INDEX].equals("Zebra"))) {
            throw new InvalidInputsException();
        }

        try {
            float weight = Float.parseFloat(parameters[WEIGHT_INDEX]);
            float speed = Float.parseFloat(parameters[SPEED_INDEX]);
            float energy = Float.parseFloat(parameters[ENERGY_INDEX]);

            switch (parameters[TYPE_INDEX]) {
                case "Boar":
                    return new Boar(weight, speed, energy, id);
                case "Lion":
                    return new Lion(weight, speed, energy, id);
                default:
                    return new Zebra(weight, speed, energy, id);
            }
        } catch (NumberFormatException e) {
            throw new InvalidInputsException();
        }
    }

    private static List<Animal> removeDeadAnimals(List<Animal> animals) {
        // to filter dead ones
        return animals.stream().filter(x -> x.getEnergy() != 0).collect(Collectors.toList());
    }

    private static void printAnimals(List<Animal> animals) {
        for (Animal animal: animals) {
            animal.makeSound();
        }
    }

    public static void main(String[] args) {
        try {
            String stD = scanner.nextLine();
            String stG = scanner.nextLine();
            String stN = scanner.nextLine();

            int days = Integer.parseInt(stD);
            if (!(days >= MINIMAL_DAYS && days <= MAXIMAL_DAYS)) {
                throw new InvalidInputsException();
            }

            float grassAmount = Float.parseFloat(stG);
            Field field = new Field(grassAmount);

            int animalsCnt = Integer.parseInt(stN);
            if (!(animalsCnt >= MINIMAL_ANIMALS && animalsCnt <= MAXIMAL_ANIMALS)) {
                throw new InvalidInputsException();
            }

            List<Animal> animals = new ArrayList<>();
            // animals input
            for (int i = 0; i < animalsCnt; i++) {
                try {
                    animals.add(receiveAnimal(scanner.nextLine().replace("\n", ""), i));
                } catch (RuntimeException e) {
                    throw new InvalidInputsException();
                }
            }

            animals = removeDeadAnimals(animals);

            // executing each day
            for (int i = 0; i < days; i++) {
                for (Animal animal: animals) {
                    try {
                        animal.eat(animals, field);
                    } catch (SelfHuntingException | CannibalismException | TooStrongPreyException e) {
                        System.out.println(e.getMessage());
                    }
                }

                // daily routines
                field.grassGrow();
                for (Animal animal: animals) {
                    animal.decrementEnergy();
                }
                animals = removeDeadAnimals(animals);
            }

            printAnimals(animals);  // final output

        } catch (RuntimeException e) {
            System.out.println(new InvalidInputsException().getMessage());
        } catch (GrassOutOfBoundsException | InvalidNumberOfAnimalParametersException
                 | InvalidInputsException | WeightOutOfBoundsException
                 | SpeedOutOfBoundsException | EnergyOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }
}

/**
 * represents sound of an animal
 */
enum AnimalSound {
    /**
     * sounds for Lion, Zebra, Boar respectively
     */
    LION_SOUND("Roar"), ZEBRA_SOUND("Ihoho"), BOAR_SOUND("Oink");

    private final String sound;

    /**
     * creates object of animal sound with specified sound
     * @param st sound
     */
    AnimalSound(String st) {
        this.sound = st;
    }

    /**
     * getter for sound.
     * @return sound as a string
     */
    public String getSound() {
        return this.sound;
    }
}

/**
 * represents field of the grass
 */
class Field {
    private float grassAmount;

    // bounds for grass amount
    private static final float MINIMAL_AMOUNT = 0;
    private static final float MAXIMAL_AMOUNT = 100;

    /**
     * creates Field
     * @param amount amount of grass on the field initially
     */
    Field(float amount) throws GrassOutOfBoundsException {
        if (!(amount >= MINIMAL_AMOUNT && amount <= MAXIMAL_AMOUNT)) {
            throw new GrassOutOfBoundsException();
        }
        this.grassAmount = amount;
    }

    /**
     * used to increase amount of grass at the end of the day
     */
    public void grassGrow() {
        this.grassAmount *= 2;
        if (this.grassAmount > MAXIMAL_AMOUNT) {
            this.grassAmount = MAXIMAL_AMOUNT;
        }
    }

    /**
     * getter for grass amount
     * @return amount of grass
     */
    public float getGrassAmount() {
        return grassAmount;
    }
    /**
     * setter for grass amount
     * @param g amount of grass
     */
    public void setGrassAmount(float g) {
        this.grassAmount = g;
    }
}

/**
 * represents abstract animal.
 */
abstract class Animal {
    private final float weight;
    private final float speed;
    private float energy;
    private final int id;  // unique number of an animal

    // constraints
    private static final float MINIMAL_WEIGHT = 5;
    private static final float MAXIMAL_WEIGHT = 200;
    private static final float MINIMAL_ENERGY = 0;
    private static final float MAXIMAL_ENERGY = 100;
    private static final float MINIMAL_SPEED = 5;
    private static final float MAXIMAL_SPEED = 60;

    /**
     * creates abstract animal
     * @param w weight
     * @param s speed
     * @param e energy
     * @param i unique index
     * @throws WeightOutOfBoundsException if weight is invalid
     * @throws EnergyOutOfBoundsException if energy is invalid
     * @throws SpeedOutOfBoundsException if speed is invalid
     */
    protected Animal(float w, float s, float e, int i)
            throws WeightOutOfBoundsException, EnergyOutOfBoundsException, SpeedOutOfBoundsException {
        if (!(w >= MINIMAL_WEIGHT && w <= MAXIMAL_WEIGHT)) {
            throw new WeightOutOfBoundsException();
        }
        if (!(s >= MINIMAL_SPEED && s <= MAXIMAL_SPEED)) {
            throw new SpeedOutOfBoundsException();
        }
        if (!(e >= MINIMAL_ENERGY && e <= MAXIMAL_ENERGY)) {
            throw new EnergyOutOfBoundsException();
        }

        this.weight = w;
        this.speed = s;
        this.energy = e;
        this.id = i;
    }

    /**
     * makes sound of an animal
     */
    public abstract void makeSound();

    /**
     * tries to eat either the animal in front, or grass in field
     * @param animals list of animals
     * @param field grass field
     */
    public abstract void eat(List<Animal> animals, Field field)
            throws CannibalismException, SelfHuntingException, TooStrongPreyException;

    /**
     * used to decrease energy by 1 at the end of the day
     */
    public void decrementEnergy() {
        this.energy--;
        if (this.energy < MINIMAL_ENERGY) {
            this.energy = MINIMAL_ENERGY;
        }
    }

    /**
     * getter for id
     * @return unique animal number
     */
    public int getId() {
        return this.id;
    }

    /**
     * getter for weight
     * @return weight of the animal
     */
    public float getWeight() {
        return this.weight;
    }
    /**
     * getter for speed
     * @return speed of the animal
     */
    public float getSpeed() {
        return this.speed;
    }
    /**
     * getter for energy
     * @return energy of the animal
     */
    public float getEnergy() {
        return energy;
    }
    /**
     * setter for energy
     * @param e new energy value
     */
    public void setEnergy(float e) {
        this.energy = e;
        if (this.energy > MAXIMAL_ENERGY) {
            this.energy = MAXIMAL_ENERGY;
        }
    }
}

/**
 * represents behaviour of the carnivore animal
 */
interface Carnivore {
    /**
     * used to get Prey from list (previous animal in the list of animals)
     * @param animalList list of animals
     * @param hunter who hunts
     * @return who is the prey
     */
    default Animal choosePrey(List<Animal> animalList, Animal hunter) {
        int hunterIndex = 0;  // index of the hunter in the list of animals
        int hunterId = hunter.getId();  // unique id of the hunter

        // getting index of the hunter
        Animal currentAnimal = animalList.get(hunterIndex);
        while (currentAnimal.getId() != hunterId) {
            hunterIndex++;
            currentAnimal = animalList.get(hunterIndex);
        }

        if (currentAnimal.getEnergy() == 0) {  // hunter is already dead
            return null;
        }

        if (hunterIndex == animalList.size() - 1) {  // if hunter is the last in line
            return animalList.get(0);
        } else {
            return animalList.get(hunterIndex + 1);
        }
    }

    /**
     * used to perform hunt action
     * @param hunter who hunts
     * @param prey who is hunted
     * @throws SelfHuntingException if hunter also is a prey (only one animal left in the list)
     * @throws CannibalismException if prey is of the same type as the hunter
     * @throws TooStrongPreyException if prey managed to escape
     */
    default void huntPrey(Animal hunter, Animal prey)
            throws SelfHuntingException, CannibalismException, TooStrongPreyException {
        if (hunter.getId() == prey.getId()) {  // self-eating
            throw new SelfHuntingException();
        }
        if (hunter.getClass().getName().equals(prey.getClass().getName())) {  // cannibalism
            throw new CannibalismException();
        }
        if (hunter.getEnergy() <= prey.getEnergy() && hunter.getSpeed() <= prey.getSpeed()) {
            // unable to catch prey
            throw new TooStrongPreyException();
        }

        // other cases: hunting is possible
        hunter.setEnergy(hunter.getEnergy() + prey.getWeight());
        prey.setEnergy(0);
    }
}

/**
 * represents behaviour of the herbivore animal
 */
interface Herbivore {
    /**
     * used to perform eating of the grass
     * @param animal who eats grass
     * @param field field of the grass
     */
    default void grazeInTheField(Animal animal, Field field) {
        if (animal.getEnergy() == 0) {  // animal is already dead
            return;
        }
        float energyGain = 0.1f * animal.getWeight();
        if (field.getGrassAmount() > energyGain) {
            field.setGrassAmount(field.getGrassAmount() - energyGain);
            animal.setEnergy(animal.getEnergy() + energyGain);
        }
    }
}

/**
 * represents Lion
 */
class Lion extends Animal implements Carnivore {
    /**
     * creates Lion
     * @param w weight
     * @param s speed
     * @param e energy
     * @param i unique id
     * @throws WeightOutOfBoundsException if weight does not satisfy constraints
     * @throws EnergyOutOfBoundsException if energy does not satisfy constraints
     * @throws SpeedOutOfBoundsException if speed  does not satisfy constraints
     */
    Lion(float w, float s, float e, int i)
            throws WeightOutOfBoundsException, EnergyOutOfBoundsException, SpeedOutOfBoundsException {
        super(w, s, e, i);
    }

    /**
     * represents lion eat
     * @param animals list of animals
     * @param field grass field
     */
    @Override
    public void eat(List<Animal> animals, Field field)
            throws CannibalismException, SelfHuntingException, TooStrongPreyException {
        Animal prey = choosePrey(animals, this);
        if (prey == null) {  // hunter is already dead
            return;
        }
        huntPrey(this, prey);
    }

    /**
     * produces sound of a Lion
     */
    @Override
    public void makeSound() {
        System.out.println(AnimalSound.LION_SOUND.getSound());
    }
}

/**
 * represents Zebra
 */
class Zebra extends Animal implements Herbivore {
    /**
     * creates Zebra
     * @param w weight
     * @param s speed
     * @param e energy
     * @param i unique id
     * @throws WeightOutOfBoundsException if weight does not satisfy constraints
     * @throws EnergyOutOfBoundsException if energy does not satisfy constraints
     * @throws SpeedOutOfBoundsException if speed  does not satisfy constraints
     */
    Zebra(float w, float s, float e, int i)
            throws WeightOutOfBoundsException, EnergyOutOfBoundsException, SpeedOutOfBoundsException {
        super(w, s, e, i);
    }

    /**
     * represents Zebra eat
     * @param animals list of animals
     * @param field grass field
     */
    @Override
    public void eat(List<Animal> animals, Field field) {
        grazeInTheField(this, field);
    }

    /**
     * produces sound of a Zebra
     */
    @Override
    public void makeSound() {
        System.out.println(AnimalSound.ZEBRA_SOUND.getSound());
    }
}

/**
 * represents Boar
 */
class Boar extends Animal implements Carnivore, Herbivore {
    /**
     * creates Boar
     * @param w weight
     * @param s speed
     * @param e energy
     * @param i unique id
     * @throws WeightOutOfBoundsException if weight does not satisfy constraints
     * @throws EnergyOutOfBoundsException if energy does not satisfy constraints
     * @throws SpeedOutOfBoundsException if speed  does not satisfy constraints
     */
    Boar(float w, float s, float e, int i)
            throws WeightOutOfBoundsException, EnergyOutOfBoundsException, SpeedOutOfBoundsException {
        super(w, s, e, i);
    }

    /**
     * represents Boar eat
     * @param animals list of animals
     * @param field grass field
     */
    @Override
    public void eat(List<Animal> animals, Field field)
            throws CannibalismException, SelfHuntingException, TooStrongPreyException {
        grazeInTheField(this, field);

        Animal prey = choosePrey(animals, this);
        if (prey == null) {  // hunter is already dead
            return;
        }
        huntPrey(this, prey);
    }

    /**
     * produces sound of a Boar
     */
    @Override
    public void makeSound() {
        System.out.println(AnimalSound.BOAR_SOUND.getSound());
    }
}

// exceptions

class GrassOutOfBoundsException extends Exception {
    @Override
    public String getMessage() {
        return "The grass is out of bounds";
    }
}

class InvalidNumberOfAnimalParametersException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of animal parameters";
    }
}

class InvalidInputsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid inputs";
    }
}

class WeightOutOfBoundsException extends Exception {
    @Override
    public String getMessage() {
        return "The weight is out of bounds";
    }
}

class SpeedOutOfBoundsException extends Exception {
    @Override
    public String getMessage() {
        return "The speed is out of bounds";
    }
}

class EnergyOutOfBoundsException extends Exception {
    @Override
    public String getMessage() {
        return "The energy is out of bounds";
    }
}

class SelfHuntingException extends Exception {
    @Override
    public String getMessage() {
        return "Self-hunting is not allowed";
    }
}

class CannibalismException extends Exception {
    @Override
    public String getMessage() {
        return "Cannibalism is not allowed";
    }
}

class TooStrongPreyException extends Exception {
    @Override
    public String getMessage() {
        return "The prey is too strong or too fast to attack";
    }
}
