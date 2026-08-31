import { test, expect } from '@playwright/test';

const senha7 = 'Aa1' + 'a'.repeat(4); // 7 caracteres — abaixo do mínimo
const senha8 = 'Aa1' + 'a'.repeat(5); // 8 caracteres — limite mínimo
const senha20 = 'Aa1' + 'a'.repeat(17); // 20 caracteres — limite máximo
const senha21 = 'Aa1' + 'a'.repeat(18); // 21 caracteres — acima do máximo

const casos = [
  { senha: senha7, classe: 'abaixo do tamanho mínimo' },
  { senha: senha8, classe: 'limite mínimo de tamanho', valida: true },
  { senha: senha20, classe: 'limite máximo de tamanho', valida: true },
  { senha: senha21, classe: 'acima do tamanho máximo' },
  { senha: 'aa1aaaaa', classe: 'sem letra maiúscula' },
  { senha: 'AA1AAAAA', classe: 'sem letra minúscula' },
  { senha: 'Aaaaaaaa', classe: 'sem dígito' },
  { senha: 'Aa1 aaaa', classe: 'contém espaço' },
  { senha: '', classe: 'vazia' },
];

for (const caso of casos) {
  test(`senha "${caso.senha || '(vazia)'}" — ${caso.classe}`, async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(caso.senha);
    await page.getByLabel('Confirmar senha').fill(caso.senha);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();

    if (caso.valida) {
      await expect(resultado).toHaveText('Senha cadastrada');
      await expect(resultado).toHaveAttribute('role', 'status');
    } else {
      await expect(resultado).toHaveText('Senha fora do padrão');
      await expect(resultado).toHaveAttribute('role', 'alert');
    }
  });
}

test.describe('senha funcional', () => {
  test('cadastra com sucesso e limpa o formulário', async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha8);
    await page.getByLabel('Confirmar senha').fill(senha8);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('Senha cadastrada');
    await expect(resultado).toHaveAttribute('role', 'status');
    await expect(page.getByLabel('Nova senha')).toHaveValue('');
    await expect(page.getByLabel('Confirmar senha')).toHaveValue('');
  });

  test('nega quando confirmação não coincide', async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha8);
    await page.getByLabel('Confirmar senha').fill('Bb2' + 'b'.repeat(5));
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toHaveText('As senhas não coincidem');
    await expect(resultado).toHaveAttribute('role', 'alert');
  });
});
