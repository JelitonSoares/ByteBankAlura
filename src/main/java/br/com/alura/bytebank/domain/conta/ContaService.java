package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

public class ContaService {

    private ConnectionFactory connection;

    public ContaService() {
        connection = new ConnectionFactory();
    }

    public Set<Conta> listarContasAbertas() {
        Connection conn = connection.getConnection();
        return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (!conta.getAtiva()) {
            throw new RegraDeNegocioException("Conta está inativa!");
        }
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.getConnection();
        new ContaDAO(conn).abrir(dadosDaConta);
     }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        if (!conta.getAtiva()) {
            throw new RegraDeNegocioException("Conta está inativa!");
        }

        BigDecimal novoValor = conta.getSaldo().subtract(valor);
        Connection conn = connection.getConnection();
        new ContaDAO(conn).atualizar(conta.getNumero(), novoValor);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }

        if (!conta.getAtiva()) {
            throw new RegraDeNegocioException("Conta está inativa!");
        }


        BigDecimal novoValor = conta.getSaldo().add(valor);
        Connection conn = connection.getConnection();
        new ContaDAO(conn).atualizar(conta.getNumero(), novoValor);
    }

    public void realizarTransferencia(Integer numeroContaOrigem, Integer numeroContaDestino, BigDecimal valor) {
        this.realizarSaque(numeroContaOrigem, valor);
        this.realizarDeposito(numeroContaDestino, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.getConnection();
        new ContaDAO(conn).encerrar(numeroDaConta);
    }

    public void excluir(Integer numeroDaConta) {
        Conta conta = buscarContaPorNumero(numeroDaConta);
        if (conta.getAtiva()) {
            throw new RegraDeNegocioException("Contas ativas não podem ser excluidas!!");
        }

        Connection conn = connection.getConnection();
        new ContaDAO(conn).excluir(conta.getNumero());
    }

    private Conta buscarContaPorNumero(Integer numero) {
        Connection conn = connection.getConnection();
        Conta conta = new ContaDAO(conn).listarPorNumero(numero);
        if(conta != null) {
            return conta;
        }
        throw new RegraDeNegocioException("Não existe conta cadastrada com esse numero!!");
    }
}
