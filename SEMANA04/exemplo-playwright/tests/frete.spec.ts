import { test, expect } from '@playwright/test';

const casos = [
  { cep: '80000000', valor: '150,00', esperado: 'Frete: R$ 15,00', classe: 'CEP iniciado em 8, abaixo do frete grátis' },
  { cep: '01310000', valor: '150,00', esperado: 'Frete: R$ 25,00', classe: 'CEP não iniciado em 8, abaixo do frete grátis' },
  { cep: '01310000', valor: '199,99', esperado: 'Frete: R$ 25,00', classe: 'valor logo abaixo do limite de frete grátis' },
  { cep: '01310000', valor: '200,00', esperado: 'Frete grátis', classe: 'valor no limite mínimo de frete grátis' },
  { cep: '80000000', valor: '200,00', esperado: 'Frete grátis', classe: 'CEP iniciado em 8 com frete grátis por valor' },
  { cep: '01310000', valor: '250,00', esperado: 'Frete grátis', classe: 'valor acima do limite de frete grátis' },
  { cep: '8000000', valor: '150,00', esperado: 'Dados inválidos', classe: 'CEP com menos de 8 dígitos' },
  { cep: '800000000', valor: '150,00', esperado: 'Dados inválidos', classe: 'CEP com mais de 8 dígitos' },
  { cep: 'abcdefgh', valor: '150,00', esperado: 'Dados inválidos', classe: 'CEP não numérico' },
  { cep: '01310000', valor: 'abc', esperado: 'Dados inválidos', classe: 'valor não numérico' },
  { cep: '01310000', valor: '150,999', esperado: 'Dados inválidos', classe: 'valor com mais de duas casas decimais' },
  { cep: '01310000', valor: '0', esperado: 'Dados inválidos', classe: 'valor igual a zero' },
  { cep: '', valor: '150,00', esperado: 'Dados inválidos', classe: 'CEP vazio' },
  { cep: '01310000', valor: '', esperado: 'Dados inválidos', classe: 'valor vazio' },
];

for (const caso of casos) {
  const valido = caso.esperado !== 'Dados inválidos';

  test(`cep "${caso.cep || '(vazio)'}" valor "${caso.valor || '(vazio)'}" — ${caso.classe}`, async ({ page }) => {
    await page.goto('/frete');
    await page.getByLabel('CEP').fill(caso.cep);
    await page.getByLabel('Valor do pedido').fill(caso.valor);
    await page.getByRole('button', { name: 'Calcular frete' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();
    await expect(resultado).toHaveText(caso.esperado);
    await expect(resultado).toHaveAttribute('role', valido ? 'status' : 'alert');
  });
}
