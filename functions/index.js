// Firebase Cloud Functions for Aureus Banking App
// Handles automatic notifications for transactions, balance alerts, and transfers
// Using Firebase Functions Generation 1 (still supported with Node 20)
// Change to generation 1 for better compatibility

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Notification sur nouvelle transaction
 * Déclenché automatiquement quand une transaction est créée
 */
exports.sendTransactionNotification = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();

        if (!transaction) return null;

        const userId = transaction.userId;

        // Récupérer les tokens FCM de l'utilisateur
        const tokensSnapshot = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('fcmTokens')
            .get();

        const tokens = [];
        tokensSnapshot.forEach(doc => {
            tokens.push(doc.id);
        });

        if (tokens.length === 0) {
            console.log('No FCM tokens found for user:', userId);
            return null;
        }

        // Déterminer le type de notification (REVENUE ou DEPENSE)
        const isIncome = transaction.type === 'INCOME';
        const emoji = isIncome ? '💰' : '💸';
        const title = isIncome ? 'Money Received!' : 'Money Spent';

        // Préparer notification
        const message = {
            notification: {
                title: `${emoji} ${title}`,
                body: `${transaction.title}: ${transaction.amount} MAD`
            },
            data: {
                type: 'transaction',
                title: transaction.title,
                amount: transaction.amount.toString(),
                transaction_type: transaction.type || 'EXPENSE',
                transaction_id: transaction.transactionId || context.params.transactionId
            },
            tokens: tokens
        };

        // Envoyer notification
        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log(`${response.successCount} transaction notifications sent successfully`);

            // Nettoyer les tokens invalides
            if (response.failureCount > 0) {
                const failedTokens = [];
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        failedTokens.push(tokens[idx]);
                    }
                });

                await admin.firestore()
                    .collection('users')
                    .doc(userId)
                    .collection('fcmTokens')
                    .where('token', 'in', failedTokens)
                    .get()
                    .then((snapshot) => {
                        const batch = admin.firestore().batch();
                        snapshot.forEach(doc => batch.delete(doc.ref));
                        return batch.commit();
                    });

                console.log(`Cleaned up ${failedTokens.length} invalid tokens`);
            }
        } catch (error) {
            console.error('Error sending transaction notification:', error);
        }

        return null;
    });

/**
 * Alert solde bas
 * Déclenché quand le solde d'un compte descend sous un seuil critique
 */
exports.checkBalanceAndSendAlert = functions.firestore
    .document('accounts/{accountId}')
    .onWrite(async (change, context) => {
        const after = change.after.data();

        if (!after) return null;

        const newBalance = after.balance;
        const userId = after.userId;

        // Si solde < 1000 MAD et > 0, envoyer alert
        const LOW_BALANCE_THRESHOLD = 1000;

        if (newBalance < LOW_BALANCE_THRESHOLD && newBalance > 0) {
            const tokensSnapshot = await admin.firestore()
                .collection('users')
                .doc(userId)
                .collection('fcmTokens')
                .get();

            const tokens = [];
            tokensSnapshot.forEach(doc => {
                tokens.push(doc.id);
            });

            if (tokens.length === 0) {
                console.log('No FCM tokens found for low balance alert');
                return null;
            }

            const message = {
                notification: {
                    title: '⚠️ Low Balance Alert',
                    body: `Your balance is ${newBalance} MAD`
                },
                data: {
                    type: 'balance_alert',
                    balance: newBalance.toString(),
                    threshold: LOW_BALANCE_THRESHOLD.toString()
                },
                tokens: tokens
            };

            try {
                const response = await admin.messaging().sendMulticast(message);
                console.log(`${response.successCount} low balance alerts sent`);
            } catch (error) {
                console.error('Error sending low balance alert:', error);
            }
        }

        return null;
    });

/**
 * Notification de transfert reçu
 * Déclenché quand un transfert est créé pour l'utilisateur
 */
exports.sendTransferNotification = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();

        if (!transaction || transaction.type !== 'INCOME') return null;

        // Vérifier si c'est un transfert (avec recipientName ou direction)
        if (!transaction.recipientName && !transaction.direction) return null;

        const userId = transaction.userId;

        const tokensSnapshot = await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('fcmTokens')
            .get();

        const tokens = [];
        tokensSnapshot.forEach(doc => {
            tokens.push(doc.id);
        });

        if (tokens.length === 0) return null;

        const direction = 'received';
        const fromTo = transaction.recipientName || transaction.senderName || 'Someone';

        const message = {
            notification: {
                title: '💰 Money Received!',
                body: `You received ${transaction.amount} MAD from ${fromTo}`
            },
            data: {
                type: 'transfer',
                amount: transaction.amount.toString(),
                direction: direction,
                from_to: fromTo
            },
            tokens: tokens
        };

        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log(`${response.successCount} transfer notifications sent`);
        } catch (error) {
            console.error('Error sending transfer notification:', error);
        }

        return null;
    });

/**
 * Nettoyage des anciens tokens FCM (executed daily)
 */
exports.cleanupOldTokens = functions.pubsub
    .schedule('0 2 * * *') // Tous les jours à 2h du matin
    .timeZone('Africa/Casablanca')
    .onRun(async (context) => {
        const cutoffDate = new Date();
        cutoffDate.setDate(cutoffDate.getDate() - 30); // 30 days

        const usersSnapshot = await admin.firestore().collection('users').get();

        const batchPromises = [];

        usersSnapshot.forEach(userDoc => {
            const promise = admin.firestore()
                .collection('users')
                .doc(userDoc.id)
                .collection('fcmTokens')
                .where('createdAt', '<', cutoffDate)
                .get()
                .then(snapshot => {
                    const batch = admin.firestore().batch();
                    snapshot.forEach(doc => batch.delete(doc.ref));
                    if (!snapshot.empty) {
                        return batch.commit();
                    }
                    return Promise.resolve();
                });

            batchPromises.push(promise);
        });

        await Promise.all(batchPromises);
        console.log('Old FCM tokens cleanup completed');
        return null;
    });

/**
 * Envoyer une notification de bienvenue après inscription
 */
exports.sendWelcomeNotification = functions.auth.user()
    .onCreate(async (user) => {
        // Wait that les données utilisateur soient créées
        await new Promise(resolve => setTimeout(resolve, 3000));

        const userDoc = await admin.firestore()
            .collection('users')
            .doc(user.uid)
            .get();

        if (!userDoc.exists) return null;

        const tokensSnapshot = await admin.firestore()
            .collection('users')
            .doc(user.uid)
            .collection('fcmTokens')
            .get();

        const tokens = [];
        tokensSnapshot.forEach(doc => {
            tokens.push(doc.id);
        });

        if (tokens.length === 0) return null;

        const firstName = userDoc.data()?.firstName || 'User';

        const message = {
            notification: {
                title: 'Welcome to Aureus Banking! 🎉',
                body: `Hello ${firstName}! Your account is ready. Start managing your finances today.`
            },
            data: {
                type: 'welcome',
                user_id: user.uid
            },
            tokens: tokens
        };

        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log(`${response.successCount} welcome notifications sent`);
        } catch (error) {
            console.error('Error sending welcome notification:', error);
        }

        return null;
    });

/**
 * Notification de mise à jour de profil
 */
exports.sendProfileUpdateNotification = functions.firestore
    .document('users/{userId}')
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();

        // Ignorer les mises à jour mineures (seulement lastLogin)
        if (Object.keys(before).length === Object.keys(after).length) {
            return null;
        }

        const tokensSnapshot = await admin.firestore()
            .collection('users')
            .doc(context.params.userId)
            .collection('fcmTokens')
            .get();

        const tokens = [];
        tokensSnapshot.forEach(doc => {
            tokens.push(doc.id);
        });

        if (tokens.length === 0) return null;

        const message = {
            notification: {
                title: 'Profile Updated',
                body: 'Your profile has been successfully updated'
            },
            data: {
                type: 'profile_update',
                user_id: context.params.userId
            },
            tokens: tokens
        };

        try {
            await admin.messaging().sendMulticast(message);
        } catch (error) {
            console.error('Error sending profile update notification:', error);
        }

        return null;
    });

/**
 * Phase 1.1: Execute Wallet Transfer - Fonction atomique de transfert
 * Débiteur envoyeur, crédite receveur, crée 2 transactions
 */
exports.executeWalletTransfer = functions.https.onCall(async (data, context) => {
    // ==================== VALIDATION AUTH ====================
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated to transfer money'
        );
    }

    const senderUserId = context.auth.uid;

    // ==================== VALIDATION INPUTS ====================
    const { recipientUserId, amount, description } = data;

    console.log(`[PHASE 10 LOG] Transfer initiated: ${senderUserId} -> ${recipientUserId}, amount: ${amount} MAD`);

    // Validation des champs requis
    if (!recipientUserId || !amount || amount <= 0) {
        console.error(`[PHASE 10 LOG] Transfer validation failed: Missing or invalid fields for user ${senderUserId}`);
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Recipient user ID and valid amount are required'
        );
    }

    // Validation: ne pas transférer à soi-même
    if (senderUserId === recipientUserId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Cannot transfer money to yourself'
        );
    }

    // Validation du montant maximum
    const MAX_TRANSFER_AMOUNT = 50000; // 50,000 MAD par transfert
    const DAILY_TRANSFER_LIMIT = 20000; // 20,000 MAD par jour

    if (amount > MAX_TRANSFER_AMOUNT) {
        console.error(`[PHASE 10 LOG] Transfer amount ${amount} exceeds maximum limit for user ${senderUserId}`);
        throw new functions.https.HttpsError(
            'invalid-argument',
            `Transfer amount exceeds maximum limit of ${MAX_TRANSFER_AMOUNT} MAD`
        );
    }

    const db = admin.firestore();

    console.log(`[PHASE 10 LOG] Starting transaction for transfer from ${senderUserId} to ${recipientUserId}`);

    try {
        // ==================== TRANSACTION ATOMIQUE ====================
        const result = await db.runTransaction(async (transaction) => {
            // 1. Récupérer le compte envoyeur
            const senderAccountsRef = db.collection('accounts')
                .where('userId', '==', senderUserId)
                .where('isActive', '==', true)
                .limit(1);

            const senderAccountsDoc = await transaction.get(senderAccountsRef);

            if (senderAccountsDoc.empty) {
                throw new functions.https.HttpsError(
                    'not-found',
                    'Sender account not found'
                );
            }

            const senderAccountId = senderAccountsDoc.docs[0].id;
            const senderAccount = senderAccountsDoc.docs[0].data();

            // 2. Récupérer le compte receveur
            const recipientAccountsRef = db.collection('accounts')
                .where('userId', '==', recipientUserId)
                .where('isActive', '==', true)
                .limit(1);

            const recipientAccountsDoc = await transaction.get(recipientAccountsRef);

            if (recipientAccountsDoc.empty) {
                throw new functions.https.HttpsError(
                    'not-found',
                    'Recipient account not found'
                );
            }

            const recipientAccountId = recipientAccountsDoc.docs[0].id;
            const recipientAccount = recipientAccountsDoc.docs[0].data();

            // 3. Vérifier le solde envoyeur
            const senderBalance = senderAccount.balance || 0;

            if (senderBalance < amount) {
                throw new functions.https.HttpsError(
                    'failed-precondition',
                    'Insufficient balance'
                );
            }

            // 4. Vérifier les quotas journaliers
            const today = admin.firestore.Timestamp.now().toDate();
            today.setHours(0, 0, 0, 0);

            const dailyTransfersQuery = db.collection('transactions')
                .where('senderUserId', '==', senderUserId)
                .where('type', '==', 'EXPENSE')
                .where('category', '==', 'Transfer')
                .where('createdAt', '>=', today);

            const dailyTransfersDoc = await transaction.get(dailyTransfersQuery);
            const dailyTotal = dailyTransfersDoc.docs.reduce((sum, doc) => {
                return sum + (doc.data().amount || 0);
            }, 0);

            const DAILY_TRANSFER_LIMIT = 20000; // 20,000 MAD par jour

            if (dailyTotal + amount > DAILY_TRANSFER_LIMIT) {
                throw new functions.https.HttpsError(
                    'failed-precondition',
                    `Daily transfer limit exceeded. Available: ${DAILY_TRANSFER_LIMIT - dailyTotal} MAD`
                );
            }

            // ==================== EXÉCUTION DU TRANSFERT ====================
            console.log(`[PHASE 10 LOG] Executing transfer: Debiting sender and crediting recipient`);

            // 5. Débiter le compte envoyeur
            const newSenderBalance = senderBalance - amount;
            transaction.update(db.collection('accounts').doc(senderAccountId), {
                balance: newSenderBalance,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // 6. Créditer le compte receveur
            const newRecipientBalance = (recipientAccount.balance || 0) + amount;
            transaction.update(db.collection('accounts').doc(recipientAccountId), {
                balance: newRecipientBalance,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            // 7. Créer transaction pour l'envoyeur (EXPENSE)
            const senderTransactionId = `trx_${Date.now()}_sender_${senderUserId}`;
            const senderTransaction = {
                transactionId: senderTransactionId,
                userId: senderUserId,
                accountId: senderAccountId,
                senderUserId: senderUserId,
                recipientUserId: recipientUserId,
                recipientName: description || 'Transfer',
                type: 'EXPENSE',
                category: 'Transfer',
                title: 'Money Sent',
                description: `Transfer to ${recipientUserId}`,
                amount: amount,
                merchant: 'Wallet Transfer',
                status: 'COMPLETED',
                balanceAfter: newSenderBalance,
                paymentMethod: 'wallet',
                direction: 'outgoing',
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            };

            transaction.set(
                db.collection('transactions').doc(senderTransactionId),
                senderTransaction
            );

            // 8. Créer transaction pour le receveur (INCOME)
            const recipientTransactionId = `trx_${Date.now()}_recipient_${recipientUserId}`;
            const recipientTransaction = {
                transactionId: recipientTransactionId,
                userId: recipientUserId,
                accountId: recipientAccountId,
                senderUserId: senderUserId,
                recipientUserId: recipientUserId,
                recipientName: description || '',
                type: 'INCOME',
                category: 'Transfer',
                title: 'Money Received',
                description: `Received from ${senderUserId}`,
                amount: amount,
                merchant: 'Wallet Transfer',
                status: 'COMPLETED',
                balanceAfter: newRecipientBalance,
                paymentMethod: 'wallet',
                direction: 'incoming',
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            };

            transaction.set(
                db.collection('transactions').doc(recipientTransactionId),
                recipientTransaction
            );

            // ==================== RÉPONSE ====================
            console.log(`[PHASE 10 LOG] Transfer completed successfully: ${senderUserId} -> ${recipientUserId}, amount: ${amount} MAD`);

            return {
                success: true,
                transactionId: senderTransactionId,
                recipientTransactionId: recipientTransactionId,
                senderBalance: newSenderBalance,
                recipientBalance: newRecipientBalance,
                amount: amount,
                timestamp: admin.firestore.FieldValue.serverTimestamp()
            };
        });

        // ==================== NOTIFICATION RECEVEUR ====================
        // Envoyer notification push au receveur après succès
        console.log(`[PHASE 10 LOG] Sending transfer notification to recipient: ${recipientUserId}`);
        const recipientTokensSnapshot = await db.collection('users')
            .doc(recipientUserId)
            .collection('fcmTokens')
            .get();

        const fcmTokens = [];
        recipientTokensSnapshot.forEach(doc => {
            fcmTokens.push(doc.id);
        });

        if (fcmTokens.length > 0) {
            // Récupérer infos envoyeur
            const senderDoc = await db.collection('users').doc(senderUserId).get();
            const senderData = senderDoc.data();
            const senderName = `${senderData?.firstName || ''} ${senderData?.lastName || ''}`.trim() || 'Someone';

            const message = {
                notification: {
                    title: '💰 Money Received!',
                    body: `${senderName} sent you ${amount} MAD`
                },
                data: {
                    type: 'transfer_received',
                    amount: amount.toString(),
                    senderUserId: senderUserId,
                    senderName: senderName,
                    transactionId: result.transactionId
                },
                tokens: fcmTokens
            };

            try {
                await admin.messaging().sendMulticast(message);
                console.log(`Transfer notification sent to ${fcmTokens.length} devices`);
            } catch (notifyError) {
                console.error('Failed to send transfer notification:', notifyError);
                // Notification error doesn't fail the transfer
            }
        }

        // ==================== LOG D'AUDIT ====================
        await db.collection('transferAudit').add({
            senderUserId: senderUserId,
            recipientUserId: recipientUserId,
            amount: amount,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: 'completed',
            transactionId: result.transactionId
        });

        return result;

    } catch (error) {
        console.error(`[PHASE 10 LOG] Wallet transfer FAILED for ${senderUserId} -> ${recipientUserId}:`, error);

        // Log d'erreur d'audit
        await db.collection('transferAudit').add({
            senderUserId: senderUserId,
            recipientUserId: recipientUserId,
            amount: amount,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: 'failed',
            error: error.message
        }).catch(e => console.error('Failed to log audit:', e));

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Transfer failed: ' + error.message
        );
    }
});

/**
 * Phase 1.2: createMoneyRequest - Créer une demande d'argent
 */
exports.createMoneyRequest = functions.https.onCall(async (data, context) => {
    // Validation auth
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated'
        );
    }

    const requesterUserId = context.auth.uid;
    const { recipientUserId, amount, reason } = data;

    console.log(`[PHASE 10 LOG] Money request initiated: ${requesterUserId} -> ${recipientUserId}, amount: ${amount} MAD`);

    // Validation inputs
    if (!recipientUserId || !amount || amount <= 0) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Recipient user ID and valid amount are required'
        );
    }

    // Validation: ne pas demander à soi-même
    if (requesterUserId === recipientUserId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Cannot request money from yourself'
        );
    }

    const db = admin.firestore();

    console.log(`[PHASE 10 LOG] Creating money request: ${requesterUserId} requesting from ${recipientUserId}`);

    try {
        // Créer la demande
        const requestId = `req_${Date.now()}_${requesterUserId}_${recipientUserId}`;

        const requestData = {
            requestId: requestId,
            requesterUserId: requesterUserId,
            targetUserId: recipientUserId,
            amount: amount,
            reason: reason || 'Money Request',
            status: 'PENDING',
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 days
        };

        await db.collection('moneyRequests').doc(requestId).set(requestData);

        console.log(`[PHASE 10 LOG] Money request created: ${requestId}`);

        // Notifier le destinataire
        console.log(`[PHASE 10 LOG] Sending money request notification to recipient: ${recipientUserId}`);
        const recipientTokensSnapshot = await db.collection('users')
            .doc(recipientUserId)
            .collection('fcmTokens')
            .get();

        const fcmTokens = [];
        recipientTokensSnapshot.forEach(doc => {
            fcmTokens.push(doc.id);
        });

        if (fcmTokens.length > 0) {
            // Récupérer infos demandeur
            const requesterDoc = await db.collection('users').doc(requesterUserId).get();
            const requesterData = requesterDoc.data();
            const requesterName = `${requesterData?.firstName || ''} ${requesterData?.lastName || ''}`.trim() || 'Someone';

            const message = {
                notification: {
                    title: '💸 Money Request',
                    body: `${requesterName} requested ${amount} MAD`
                },
                data: {
                    type: 'money_request',
                    amount: amount.toString(),
                    requesterUserId: requesterUserId,
                    requesterName: requesterName,
                    requestId: requestId,
                    reason: reason || ''
                },
                tokens: fcmTokens
            };

            await admin.messaging().sendMulticast(message);
            console.log(`[PHASE 10 LOG] Money request notification sent to ${fcmTokens.length} devices`);
        }

        return {
            success: true,
            requestId: requestId,
            amount: amount,
            status: 'PENDING'
        };

    } catch (error) {
        console.error(`[PHASE 10 LOG] Money request FAILED for ${requesterUserId}:`, error);

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Request failed: ' + error.message
        );
    }
});

/**
 * Phase 1.3: validateUserId - Vérifier si un userId existe
 * Utilisé dans UI pour valider contacts avant transfert
 */
exports.validateUserId = functions.https.onCall(async (data, context) => {
    const requestingUserId = context.auth ? context.auth.uid : 'anonymous';

    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated'
        );
    }

    const { userId } = data;

    console.log(`[PHASE 10 LOG] User validation requested by ${requestingUserId} for userId: ${userId}`);

    if (!userId) {
        console.error(`[PHASE 10 LOG] User validation failed: Missing userId for requester ${requestingUserId}`);
        throw new functions.https.HttpsError(
            'invalid-argument',
            'User ID is required'
        );
    }

    const db = admin.firestore();

    try {
        const userDoc = await db.collection('users').doc(userId).get();

        if (!userDoc.exists) {
            console.log(`[PHASE 10 LOG] User validation result: userId ${userId} NOT FOUND`);
            return {
                exists: false,
                userId: null
            };
        }

        const userData = userDoc.data();

        console.log(`[PHASE 10 LOG] User validation result: userId ${userId} FOUND - ${userData?.firstName || ''} ${userData?.lastName || ''}`);

        return {
            exists: true,
            userId: userId,
            firstName: userData?.firstName || '',
            lastName: userData?.lastName || '',
            email: userData?.email || '',
            phone: userData?.phone || ''
        };

    } catch (error) {
        console.error(`[PHASE 10 LOG] User validation FAILED for userId ${userId}:`, error);

        if (error instanceof functions.https.HttpsError) {
            throw error;
        }

        throw new functions.https.HttpsError(
            'internal',
            'Validation failed: ' + error.message
        );
    }
});