package ntu.mdp.group.three.astarpathfinder;

// https://en.wikipedia.org/wiki/A*_search_algorithm
public class Node {

    public int g = 0;                  // g(n) where n is the RHS.
    public int h = 0;                  // h(n) where n is the RHS.
    public int cost = g + h;        // f(n) = g(n) + h(n);
    public int[] position;                       // robot's position [x, y]
    public Node parent = null;                // Parent Node pointer.

    public Node(int[] position) {
        this.position = position;
    }

    public void updateCost() {
        this.cost = this.h + this.g;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
