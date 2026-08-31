package com.example;

public class Main {
    public static void main(String[] args) {
        CalculadoraFrete calculadora = new CalculadoraFrete();
    
        System.out.println(calculadora.calcular(0, false));
        System.out.println(calculadora.calcular(200, false));
        System.out.println(calculadora.calcular(100, true));
        
    }
}