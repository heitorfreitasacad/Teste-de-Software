package br.edu.exemplo;

public class BubbleSort {

    private int[] numeros;

    public BubbleSort(int[] numeros) {
        this.numeros = numeros;
    }

    public void ordenar() {
        for (int fim = numeros.length - 1; fim > 0; fim--) {
            boolean trocou = false;
            for (int i = 0; i < fim; i++) {
                if (numeros[i] > numeros[i + 1]) {
                    trocar(i, i + 1);
                    trocou = true;
                }
            }
            // Se uma passada inteira não trocou nada, o vetor já está ordenado.
            if (!trocou) {
                return;
            }
        }
    }

    private void trocar(int i, int j) {
        int temp = numeros[i];
        numeros[i] = numeros[j];
        numeros[j] = temp;
    }

    public int[] getNumeros() {
        return numeros;
    }
}
