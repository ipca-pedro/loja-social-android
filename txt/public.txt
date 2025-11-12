const express = require('express');
const pool = require('../db');
const router = express.Router();

// GET /api/public/stock-summary - Resumo público do stock
router.get('/stock-summary', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM public_stock_summary');
    res.json({
      success: true,
      data: result.rows
    });
  } catch (error) {
    console.error('Erro ao obter resumo do stock:', error);
    res.status(500).json({
      success: false,
      message: 'Erro interno do servidor'
    });
  }
});

// GET /api/public/campanhas - Lista de campanhas
router.get('/campanhas', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM campanhas ORDER BY data_inicio DESC');
    res.json({
      success: true,
      data: result.rows
    });
  } catch (error) {
    console.error('Erro ao obter campanhas:', error);
    res.status(500).json({
      success: false,
      message: 'Erro interno do servidor'
    });
  }
});

// POST /api/public/contacto - Formulário de contacto
router.post('/contacto', async (req, res) => {
  try {
    const { nome, email, mensagem } = req.body;

    // Validação básica
    if (!email || !mensagem) {
      return res.status(400).json({
        success: false,
        message: 'Email e mensagem são obrigatórios'
      });
    }

    const result = await pool.query(
      'INSERT INTO mensagens_contacto (nome, email, mensagem) VALUES ($1, $2, $3) RETURNING id',
      [nome, email, mensagem]
    );

    res.status(201).json({
      success: true,
      message: 'Mensagem enviada com sucesso',
      data: { id: result.rows[0].id }
    });
  } catch (error) {
    console.error('Erro ao enviar mensagem de contacto:', error);
    res.status(500).json({
      success: false,
      message: 'Erro interno do servidor'
    });
  }
});

module.exports = router;